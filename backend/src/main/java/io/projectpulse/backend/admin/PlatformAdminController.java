package io.projectpulse.backend.admin;

import io.projectpulse.backend.admin.dto.AdminStatsResponse;
import io.projectpulse.backend.admin.dto.AdminUserResponse;
import io.projectpulse.backend.audit.AuditLog;
import io.projectpulse.backend.common.ApiResponse;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.user.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@Tag(name = "Platform Administration", description = "System-level administrator controls, audit logs, and metrics")
public class PlatformAdminController {

    private final AdminService adminService;

    public PlatformAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform-wide aggregated telemetry and entity counts")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        AdminStatsResponse stats = adminService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered platform users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getUsers() {
        List<AdminUserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Suspend or reactivate a user account")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        adminService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success("User status updated to " + status, null));
    }

    @GetMapping("/organizations")
    @Operation(summary = "List all organizations on the platform")
    public ResponseEntity<ApiResponse<List<Organization>>> getOrganizations() {
        List<Organization> orgs = adminService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.success(orgs));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Retrieve recent system-wide audit activity logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> logs = adminService.getAuditLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
