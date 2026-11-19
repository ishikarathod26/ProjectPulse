package io.projectpulse.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "Project name is required")
    @Size(max = 150, message = "Project name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Key identifier is required (e.g. PRJ, PULSE)")
    @Pattern(regexp = "^[A-Z0-9_-]{2,20}$", message = "Key must be 2-20 uppercase alphanumeric characters, dashes, or underscores")
    private String keyIdentifier;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    public CreateProjectRequest() {}

    public CreateProjectRequest(Long organizationId, String name, String keyIdentifier, String description) {
        this.organizationId = organizationId;
        this.name = name;
        this.keyIdentifier = keyIdentifier;
        this.description = description;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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
}
