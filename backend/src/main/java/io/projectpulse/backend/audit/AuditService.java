package io.projectpulse.backend.audit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void recordAction(Long organizationId, Long userId, String action, String entityType, Long entityId, String details, String ipAddress) {
        try {
            AuditLog log = new AuditLog(organizationId, userId, action, entityType, entityId, details, ipAddress);
            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // Audit logging should never disrupt user transactions
        }
    }
}
