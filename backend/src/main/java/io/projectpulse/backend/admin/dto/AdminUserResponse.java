package io.projectpulse.backend.admin.dto;

import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.UserStatus;

import java.time.Instant;

public class AdminUserResponse {
    private Long id;
    private String email;
    private String fullName;
    private SystemRole systemRole;
    private UserStatus status;
    private Instant createdAt;

    public AdminUserResponse() {}

    public AdminUserResponse(Long id, String email, String fullName, SystemRole systemRole, UserStatus status, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.systemRole = systemRole;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public SystemRole getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(SystemRole systemRole) {
        this.systemRole = systemRole;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
