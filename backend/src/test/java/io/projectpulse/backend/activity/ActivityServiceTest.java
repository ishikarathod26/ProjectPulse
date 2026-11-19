package io.projectpulse.backend.activity;

import io.projectpulse.backend.activity.dto.ActivityBatchRequest;
import io.projectpulse.backend.activity.dto.ActivityBatchResponse;
import io.projectpulse.backend.activity.dto.ActivityEventDto;
import io.projectpulse.backend.blockchain.BlockchainRecordRepository;
import io.projectpulse.backend.blockchain.BlockchainService;
import io.projectpulse.backend.blockchain.HashService;
import io.projectpulse.backend.device.Device;
import io.projectpulse.backend.device.DeviceRepository;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.project.Project;
import io.projectpulse.backend.project.ProjectRepository;
import io.projectpulse.backend.project.ProjectService;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private BlockchainRecordRepository blockchainRecordRepository;

    private ActivityValidator activityValidator;
    private HashService hashService;
    private ActivityService activityService;

    private User user;
    private Organization org;
    private Project project;
    private Device device;

    @BeforeEach
    void setUp() {
        activityValidator = new ActivityValidator();
        hashService = new HashService();

        activityService = new ActivityService(
                activityRepository,
                projectRepository,
                projectService,
                userRepository,
                deviceRepository,
                activityValidator,
                hashService,
                blockchainService,
                blockchainRecordRepository
        );

        user = new User("dev@example.com", "hash", "Developer", SystemRole.MEMBER);
        user.setId(1L);

        org = new Organization("Org", "org", "Desc", user);
        org.setId(10L);

        project = new Project(org, "Pulse", "PULSE", "Desc", user);
        project.setId(100L);

        device = new Device(user, "dev-box-1", "Workstation", "Win11");
        device.setId(50L);
    }

    @Test
    void ingestBatch_Success() {
        ActivityEventDto event1 = new ActivityEventDto("evt-1", EventType.CREATE, "src/App.java", null, 1024L, Instant.now());
        ActivityEventDto event2 = new ActivityEventDto("evt-2", EventType.MODIFY, "src/App.java", null, 1200L, Instant.now());
        ActivityBatchRequest batchRequest = new ActivityBatchRequest(100L, "dev-box-1", List.of(event1, event2));

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(deviceRepository.findByDeviceIdentifier("dev-box-1")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        when(activityRepository.existsByDeviceIdAndClientEventId(50L, "evt-1")).thenReturn(false);
        when(activityRepository.existsByDeviceIdAndClientEventId(50L, "evt-2")).thenReturn(false);

        Activity savedAct = new Activity(org, project, user, device, "evt-1", EventType.CREATE, "src/App.java", null, 1024L, Instant.now(), "hash1");
        savedAct.setId(500L);
        when(activityRepository.save(any(Activity.class))).thenReturn(savedAct);

        ActivityBatchResponse response = activityService.ingestBatch(batchRequest, 1L);

        assertNotNull(response);
        assertEquals(2, response.getAcceptedCount());
        assertEquals(0, response.getDuplicateCount());
        assertEquals(0, response.getRejectedCount());
        verify(blockchainService, times(2)).anchorActivityHash(any(), any(), any());
    }

    @Test
    void ingestBatch_DuplicateEvent_DeduplicatedSafely() {
        ActivityEventDto event1 = new ActivityEventDto("dup-evt", EventType.MODIFY, "src/Main.java", null, 500L, Instant.now());
        ActivityBatchRequest batchRequest = new ActivityBatchRequest(100L, "dev-box-1", List.of(event1));

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(deviceRepository.findByDeviceIdentifier("dev-box-1")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        // Simulate event already saved
        when(activityRepository.existsByDeviceIdAndClientEventId(50L, "dup-evt")).thenReturn(true);

        ActivityBatchResponse response = activityService.ingestBatch(batchRequest, 1L);

        assertEquals(0, response.getAcceptedCount());
        assertEquals(1, response.getDuplicateCount());
        assertEquals(0, response.getRejectedCount());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void ingestBatch_PathTraversal_Rejected() {
        ActivityEventDto illegalEvent = new ActivityEventDto("bad-evt", EventType.CREATE, "../../etc/passwd", null, 100L, Instant.now());
        ActivityBatchRequest batchRequest = new ActivityBatchRequest(100L, "dev-box-1", List.of(illegalEvent));

        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(deviceRepository.findByDeviceIdentifier("dev-box-1")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        ActivityBatchResponse response = activityService.ingestBatch(batchRequest, 1L);

        assertEquals(0, response.getAcceptedCount());
        assertEquals(1, response.getRejectedCount());
    }
}
