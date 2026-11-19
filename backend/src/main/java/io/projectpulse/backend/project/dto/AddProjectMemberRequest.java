package io.projectpulse.backend.project.dto;

import io.projectpulse.backend.project.ProjectRole;
import jakarta.validation.constraints.NotNull;

public class AddProjectMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role is required")
    private ProjectRole role;

    public AddProjectMemberRequest() {}

    public AddProjectMemberRequest(Long userId, ProjectRole role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }
}
