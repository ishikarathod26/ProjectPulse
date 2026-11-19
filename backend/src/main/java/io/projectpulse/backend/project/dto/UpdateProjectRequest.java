package io.projectpulse.backend.project.dto;

import io.projectpulse.backend.project.ProjectStatus;
import jakarta.validation.constraints.Size;

public class UpdateProjectRequest {

    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private ProjectStatus status;

    public UpdateProjectRequest() {}

    public UpdateProjectRequest(String name, String description, ProjectStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
