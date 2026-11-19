package io.projectpulse.backend.admin;

import io.projectpulse.backend.activity.ActivityRepository;
import io.projectpulse.backend.admin.dto.AdminStatsResponse;
import io.projectpulse.backend.admin.dto.AdminUserResponse;
import io.projectpulse.backend.audit.AuditLog;
import io.projectpulse.backend.audit.AuditLogRepository;
import io.projectpulse.backend.blockchain.BlockchainRecordRepository;
import io.projectpulse.backend.common.ResourceNotFoundException;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.organization.OrganizationRepository;
import io.projectpulse.backend.project.ProjectRepository;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import io.projectpulse.backend.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final ActivityRepository activityRepository;
    private final BlockchainRecordRepository blockchainRecordRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminService(UserRepository userRepository,
                        OrganizationRepository organizationRepository,
                        ProjectRepository projectRepository,
                        ActivityRepository activityRepository,
                        BlockchainRecordRepository blockchainRecordRepository,
                        AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
        this.activityRepository = activityRepository;
        this.blockchainRecordRepository = blockchainRecordRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getSystemStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                organizationRepository.count(),
                projectRepository.count(),
                activityRepository.count(),
                blockchainRecordRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(u -> new AdminUserResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getSystemRole(),
                u.getStatus(),
                u.getCreatedAt()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
