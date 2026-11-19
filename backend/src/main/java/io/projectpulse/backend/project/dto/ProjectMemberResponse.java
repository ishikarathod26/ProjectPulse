package io.projectpulse.backend.project.dto;

import io.projectpulse.backend.project.ProjectRole;
import java.time.Instant;

public class ProjectMemberResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private ProjectRole role;
    private Instant addedAt;

    public ProjectMemberResponse() {}

    public ProjectMemberResponse(Long id, Long userId, String email, String fullName, ProjectRole role, Instant addedAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}
