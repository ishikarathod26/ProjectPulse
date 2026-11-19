package io.projectpulse.backend.activity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ActivityBatchRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Device identifier is required")
    private String deviceIdentifier;

    @NotEmpty(message = "Activity event batch cannot be empty")
    @Size(max = 200, message = "Maximum 200 events allowed per batch upload")
    private List<@Valid ActivityEventDto> events;

    public ActivityBatchRequest() {}

    public ActivityBatchRequest(Long projectId, String deviceIdentifier, List<ActivityEventDto> events) {
        this.projectId = projectId;
        this.deviceIdentifier = deviceIdentifier;
        this.events = events;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public List<ActivityEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<ActivityEventDto> events) {
        this.events = events;
    }
}
