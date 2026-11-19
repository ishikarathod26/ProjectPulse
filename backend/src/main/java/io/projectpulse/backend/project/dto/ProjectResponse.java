package io.projectpulse.backend.project.dto;

import io.projectpulse.backend.project.ProjectRole;
import io.projectpulse.backend.project.ProjectStatus;

import java.time.Instant;

public class ProjectResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String name;
    private String keyIdentifier;
    private String description;
    private ProjectStatus status;
    private Long createdById;
    private String createdByName;
    private ProjectRole currentUserRole;
    private long memberCount;
    private Instant createdAt;
    private Instant updatedAt;

    public ProjectResponse() {}

    public ProjectResponse(Long id, Long organizationId, String organizationName, String name, String keyIdentifier, String description, ProjectStatus status, Long createdById, String createdByName, ProjectRole currentUserRole, long memberCount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.name = name;
        this.keyIdentifier = keyIdentifier;
        this.description = description;
        this.status = status;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.currentUserRole = currentUserRole;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyIdentifier() {
        return keyIdentifier;
    }

    public void setKeyIdentifier(String keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public ProjectRole getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(ProjectRole currentUserRole) {
        this.currentUserRole = currentUserRole;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
