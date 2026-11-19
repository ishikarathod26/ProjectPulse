package io.projectpulse.backend.config;

import io.projectpulse.backend.activity.Activity;
import io.projectpulse.backend.activity.ActivityRepository;
import io.projectpulse.backend.activity.EventType;
import io.projectpulse.backend.blockchain.BlockchainRecord;
import io.projectpulse.backend.blockchain.BlockchainRecordRepository;
import io.projectpulse.backend.blockchain.HashService;
import io.projectpulse.backend.collaboration.Message;
import io.projectpulse.backend.collaboration.MessageRepository;
import io.projectpulse.backend.collaboration.ProjectNote;
import io.projectpulse.backend.collaboration.ProjectNoteRepository;
import io.projectpulse.backend.collaboration.StatusUpdate;
import io.projectpulse.backend.collaboration.StatusUpdateRepository;
import io.projectpulse.backend.device.Device;
import io.projectpulse.backend.device.DeviceRepository;
import io.projectpulse.backend.organization.*;
import io.projectpulse.backend.project.*;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository orgMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final DeviceRepository deviceRepository;
    private final ActivityRepository activityRepository;
    private final BlockchainRecordRepository blockchainRecordRepository;
    private final MessageRepository messageRepository;
    private final ProjectNoteRepository noteRepository;
    private final StatusUpdateRepository statusUpdateRepository;
    private final PasswordEncoder passwordEncoder;
    private final HashService hashService;

    @Value("${projectpulse.admin.initial-email:admin@projectpulse.io}")
    private String adminEmail;

    @Value("${projectpulse.admin.initial-password}")
    private String adminPassword;

    @Value("${projectpulse.admin.initial-name:System Administrator}")
    private String adminName;

    public DataInitializer(UserRepository userRepository,
                           OrganizationRepository organizationRepository,
                           OrganizationMemberRepository orgMemberRepository,
                           ProjectRepository projectRepository,
                           ProjectMemberRepository projectMemberRepository,
                           DeviceRepository deviceRepository,
                           ActivityRepository activityRepository,
                           BlockchainRecordRepository blockchainRecordRepository,
                           MessageRepository messageRepository,
                           ProjectNoteRepository noteRepository,
                           StatusUpdateRepository statusUpdateRepository,
                           PasswordEncoder passwordEncoder,
                           HashService hashService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.orgMemberRepository = orgMemberRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.deviceRepository = deviceRepository;
        this.activityRepository = activityRepository;
        this.blockchainRecordRepository = blockchainRecordRepository;
        this.messageRepository = messageRepository;
        this.noteRepository = noteRepository;
        this.statusUpdateRepository = statusUpdateRepository;
        this.passwordEncoder = passwordEncoder;
        this.hashService = hashService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Bootstrap Platform Administrator
        User adminUser = userRepository.findByEmail(adminEmail.toLowerCase().trim()).orElseGet(() -> {
            logger.info("Bootstrapping initial platform administrator: {}", adminEmail);
            User user = new User(
                    adminEmail.toLowerCase().trim(),
                    passwordEncoder.encode(adminPassword),
                    adminName,
                    SystemRole.PLATFORM_ADMIN
            );
            return userRepository.save(user);
        });

        // 2. Bootstrap Initial Workspace & Seed Demo Data if empty
        if (organizationRepository.count() == 0) {
            logger.info("Initializing baseline organization, project, and seed records for development...");

            Organization org = new Organization(
                    "Acme Engineering",
                    "acme-engineering",
                    "Primary engineering workspace for ProjectPulse demonstration",
                    adminUser
            );
            org = organizationRepository.save(org);

            OrganizationMember orgMember = new OrganizationMember(
                    org, adminUser, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE
            );
            orgMemberRepository.save(orgMember);

            Project project = new Project(
                    org,
                    "ProjectPulse Core",
                    "PULSE",
                    "Cross-platform project activity tracking and blockchain verification engine",
                    adminUser
            );
            project = projectRepository.save(project);

            ProjectMember projectMember = new ProjectMember(
                    project, adminUser, ProjectRole.PROJECT_ADMIN
            );
            projectMemberRepository.save(projectMember);

            Device device = new Device(
                    adminUser,
                    "dev-station-alpha",
                    "Primary Development Workstation",
                    "Windows 11 Pro / Java 21"
            );
            device = deviceRepository.save(device);

            // Sample activities demonstrating verified hashing
            Instant now = Instant.now();
            createSampleActivity(org, project, adminUser, device, EventType.CREATE, "src/main/App.java", null, 1420L, now.minus(3, ChronoUnit.HOURS));
            createSampleActivity(org, project, adminUser, device, EventType.MODIFY, "src/main/App.java", null, 1850L, now.minus(2, ChronoUnit.HOURS));
            createSampleActivity(org, project, adminUser, device, EventType.CREATE, "config/blockchain.json", null, 512L, now.minus(1, ChronoUnit.HOURS));

            // Sample Collaboration: Messages
            messageRepository.save(new Message(project, adminUser, "Welcome to the ProjectPulse Core workspace! Tracking agent is operational."));
            messageRepository.save(new Message(project, adminUser, "Blockchain integrity anchoring is configured and active for all filesystem commits."));

            // Sample Collaboration: Status Update
            statusUpdateRepository.save(new StatusUpdate(
                    project, adminUser, "ON_TRACK", "Sprint 1 Milestones On Track", "Backend APIs, database migrations, and desktop tracking agent initialized."
            ));

            // Sample Collaboration: Note
            noteRepository.save(new ProjectNote(
                    project, adminUser, null, "Architecture Notes", "Core activity telemetry uses SHA-256 canonical hashing before dispatching to the ledger."
            ));

            logger.info("Baseline workspace initialized successfully.");
        }
    }

    private void createSampleActivity(Organization org, Project project, User user, Device device,
                                      EventType eventType, String path, String oldPath, Long size, Instant timestamp) {
        String eventId = UUID.randomUUID().toString();
        String hash = hashService.computeActivityHash(
                org.getId(), project.getId(), user.getId(), eventId, eventType.name(), path, timestamp, size
        );

        Activity activity = new Activity(org, project, user, device, eventId, eventType, path, oldPath, size, timestamp, hash);
        activity = activityRepository.save(activity);

        BlockchainRecord record = new BlockchainRecord(activity, hash);
        record.setTxId("tx-seed-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        record.setAnchoringStatus(io.projectpulse.backend.blockchain.AnchoringStatus.ANCHORED);
        record.setAnchoredAt(timestamp.plusSeconds(3));
        blockchainRecordRepository.save(record);
    }
}
