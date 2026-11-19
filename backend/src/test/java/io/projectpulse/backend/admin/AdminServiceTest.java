package io.projectpulse.backend.admin;

import io.projectpulse.backend.activity.ActivityRepository;
import io.projectpulse.backend.admin.dto.AdminStatsResponse;
import io.projectpulse.backend.admin.dto.AdminUserResponse;
import io.projectpulse.backend.audit.AuditLogRepository;
import io.projectpulse.backend.blockchain.BlockchainRecordRepository;
import io.projectpulse.backend.organization.OrganizationRepository;
import io.projectpulse.backend.project.ProjectRepository;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import io.projectpulse.backend.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private BlockchainRecordRepository blockchainRecordRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(
                userRepository,
                organizationRepository,
                projectRepository,
                activityRepository,
                blockchainRecordRepository,
                auditLogRepository
        );
    }

    @Test
    void getSystemStats_Success() {
        when(userRepository.count()).thenReturn(15L);
        when(organizationRepository.count()).thenReturn(3L);
        when(projectRepository.count()).thenReturn(8L);
        when(activityRepository.count()).thenReturn(120L);
        when(blockchainRecordRepository.count()).thenReturn(110L);

        AdminStatsResponse stats = adminService.getSystemStats();

        assertNotNull(stats);
        assertEquals(15L, stats.getTotalUsers());
        assertEquals(3L, stats.getTotalOrganizations());
        assertEquals(8L, stats.getTotalProjects());
        assertEquals(120L, stats.getTotalActivities());
        assertEquals(110L, stats.getTotalAnchoredRecords());
    }

    @Test
    void updateUserStatus_Success() {
        User user = new User("bad@example.com", "hash", "Bad Actor", SystemRole.MEMBER);
        user.setId(9L);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        adminService.updateUserStatus(9L, UserStatus.SUSPENDED);

        assertEquals(UserStatus.SUSPENDED, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void getAllUsers_Success() {
        User u1 = new User("u1@example.com", "hash", "User One", SystemRole.MEMBER);
        u1.setId(1L);
        when(userRepository.findAll()).thenReturn(List.of(u1));

        List<AdminUserResponse> users = adminService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("User One", users.get(0).getFullName());
    }
}
