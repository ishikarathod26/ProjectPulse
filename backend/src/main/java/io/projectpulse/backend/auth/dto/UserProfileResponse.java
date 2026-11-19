package io.projectpulse.backend.auth.dto;

import io.projectpulse.backend.organization.dto.OrgSummaryDto;
import io.projectpulse.backend.user.SystemRole;

import java.time.Instant;
import java.util.List;

public class UserProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private SystemRole systemRole;
    private Instant createdAt;
    private List<OrgSummaryDto> organizations;

    public UserProfileResponse() {}

    public UserProfileResponse(Long id, String email, String fullName, SystemRole systemRole, Instant createdAt, List<OrgSummaryDto> organizations) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.systemRole = systemRole;
        this.createdAt = createdAt;
        this.organizations = organizations;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrgSummaryDto> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrgSummaryDto> organizations) {
        this.organizations = organizations;
    }
}
