package io.projectpulse.backend.activity;

import io.projectpulse.backend.activity.dto.ActivityBatchRequest;
import io.projectpulse.backend.activity.dto.ActivityBatchResponse;
import io.projectpulse.backend.activity.dto.ActivityEventDto;
import io.projectpulse.backend.activity.dto.ActivityResponse;
import io.projectpulse.backend.blockchain.BlockchainRecord;
import io.projectpulse.backend.blockchain.BlockchainRecordRepository;
import io.projectpulse.backend.blockchain.BlockchainService;
import io.projectpulse.backend.blockchain.HashService;
import io.projectpulse.backend.common.PagedResponse;
import io.projectpulse.backend.common.ResourceNotFoundException;
import io.projectpulse.backend.device.Device;
import io.projectpulse.backend.device.DeviceRepository;
import io.projectpulse.backend.project.Project;
import io.projectpulse.backend.project.ProjectRepository;
import io.projectpulse.backend.project.ProjectService;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    private final ActivityRepository activityRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final ActivityValidator activityValidator;
    private final HashService hashService;
    private final BlockchainService blockchainService;
    private final BlockchainRecordRepository blockchainRecordRepository;

    public ActivityService(ActivityRepository activityRepository,
                           ProjectRepository projectRepository,
                           ProjectService projectService,
                           UserRepository userRepository,
                           DeviceRepository deviceRepository,
                           ActivityValidator activityValidator,
                           HashService hashService,
                           BlockchainService blockchainService,
                           BlockchainRecordRepository blockchainRecordRepository) {
        this.activityRepository = activityRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.activityValidator = activityValidator;
        this.hashService = hashService;
        this.blockchainService = blockchainService;
        this.blockchainRecordRepository = blockchainRecordRepository;
    }

    @Transactional
    public ActivityBatchResponse ingestBatch(ActivityBatchRequest request, Long userId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        // Validate user belongs to the project
        projectService.validateProjectAccess(project, userId);

        User user = userRepository.findById(userId).orElseThrow();

        // Resolve or register device
        Device device = deviceRepository.findByDeviceIdentifier(request.getDeviceIdentifier().trim())
                .orElseGet(() -> {
                    Device newDev = new Device(user, request.getDeviceIdentifier().trim(), "Workstation-" + request.getDeviceIdentifier().substring(0, Math.min(8, request.getDeviceIdentifier().length())), "Desktop OS");
                    return deviceRepository.save(newDev);
                });
        device.setLastSeenAt(Instant.now());
        deviceRepository.save(device);

        int accepted = 0;
        int duplicate = 0;
        int rejected = 0;

        for (ActivityEventDto eventDto : request.getEvents()) {
            try {
                activityValidator.validateEvent(eventDto);

                // Duplicate protection / Idempotency
                if (activityRepository.existsByDeviceIdAndClientEventId(device.getId(), eventDto.getClientEventId())) {
                    duplicate++;
                    continue;
                }

                // Deterministic canonical hash calculation
                String payloadHash = hashService.computeActivityHash(
                        project.getOrganization().getId(),
                        project.getId(),
                        user.getId(),
                        eventDto.getClientEventId(),
                        eventDto.getEventType().name(),
                        eventDto.getFilePath(),
                        eventDto.getEventTimestamp(),
                        eventDto.getFileSizeBytes()
                );

                Activity activity = new Activity(
                        project.getOrganization(),
                        project,
                        user,
                        device,
                        eventDto.getClientEventId(),
                        eventDto.getEventType(),
                        eventDto.getFilePath(),
                        eventDto.getOldFilePath(),
                        eventDto.getFileSizeBytes(),
                        eventDto.getEventTimestamp(),
                        payloadHash
                );
                activity = activityRepository.save(activity);

                // Anchor in blockchain record
                blockchainService.anchorActivityHash(activity.getId(), payloadHash, "Canonical activity anchor");

                accepted++;
            } catch (Exception ex) {
                logger.warn("Failed to process event {}: {}", eventDto.getClientEventId(), ex.getMessage());
                rejected++;
            }
        }

        return new ActivityBatchResponse(
                accepted,
                duplicate,
                rejected,
                String.format("Batch ingestion complete: %d accepted, %d duplicates skipped, %d rejected", accepted, duplicate, rejected)
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<ActivityResponse> getProjectActivities(
            Long projectId, Long memberId, EventType eventType,
            Instant startDate, Instant endDate, int page, int size, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);

        Specification<Activity> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            if (memberId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), memberId));
            }
            if (eventType != null) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventTimestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventTimestamp"), endDate));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTimestamp"));
        Page<Activity> activityPage = activityRepository.findAll(spec, pageable);

        List<ActivityResponse> responses = activityPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                responses,
                activityPage.getNumber(),
                activityPage.getSize(),
                activityPage.getTotalElements(),
                activityPage.getTotalPages(),
                activityPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProjectActivityStats(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);

        long totalActivities = activityRepository.countByProjectId(projectId);
        long past24h = activityRepository.countRecentByProjectId(projectId, Instant.now().minus(24, ChronoUnit.HOURS));

        List<Object[]> typeCounts = activityRepository.countActivitiesByEventType(projectId);
        Map<String, Long> byType = new HashMap<>();
        for (EventType t : EventType.values()) {
            byType.put(t.name(), 0L);
        }
        for (Object[] row : typeCounts) {
            EventType t = (EventType) row[0];
            Long cnt = (Long) row[1];
            byType.put(t.name(), cnt);
        }

        return Map.of(
                "totalActivities", totalActivities,
                "activitiesPast24h", past24h,
                "byEventType", byType
        );
    }

    private ActivityResponse mapToResponse(Activity activity) {
        Optional<BlockchainRecord> recordOpt = blockchainRecordRepository.findByActivityId(activity.getId());
        String status = recordOpt.map(r -> r.getAnchoringStatus().name()).orElse("UNANCHORED");
        String txId = recordOpt.map(BlockchainRecord::getTxId).orElse(null);

        return new ActivityResponse(
                activity.getId(),
                activity.getProject().getId(),
                activity.getProject().getName(),
                activity.getUser().getId(),
                activity.getUser().getFullName(),
                activity.getUser().getEmail(),
                activity.getDevice() != null ? activity.getDevice().getId() : null,
                activity.getDevice() != null ? activity.getDevice().getDeviceName() : "Unknown",
                activity.getClientEventId(),
                activity.getEventType(),
                activity.getFilePath(),
                activity.getOldFilePath(),
                activity.getFileSizeBytes(),
                activity.getEventTimestamp(),
                activity.getPayloadHash(),
                status,
                txId,
                activity.getCreatedAt()
        );
    }
}
