package io.projectpulse.backend.auth.dto;

import io.projectpulse.backend.user.SystemRole;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private SystemRole systemRole;
    private Long activeOrganizationId;
    private String activeOrganizationName;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String email, String fullName, SystemRole systemRole, Long activeOrganizationId, String activeOrganizationName) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.systemRole = systemRole;
        this.activeOrganizationId = activeOrganizationId;
        this.activeOrganizationName = activeOrganizationName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Long getActiveOrganizationId() {
        return activeOrganizationId;
    }

    public void setActiveOrganizationId(Long activeOrganizationId) {
        this.activeOrganizationId = activeOrganizationId;
    }

    public String getActiveOrganizationName() {
        return activeOrganizationName;
    }

    public void setActiveOrganizationName(String activeOrganizationName) {
        this.activeOrganizationName = activeOrganizationName;
    }
}
