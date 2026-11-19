package io.projectpulse.backend.activity.dto;

import io.projectpulse.backend.activity.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class ActivityEventDto {

    @NotBlank(message = "Client event ID is required")
    private String clientEventId;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path cannot exceed 500 characters")
    private String filePath;

    @Size(max = 500, message = "Old file path cannot exceed 500 characters")
    private String oldFilePath;

    private Long fileSizeBytes;

    @NotNull(message = "Event timestamp is required")
    private Instant eventTimestamp;

    public ActivityEventDto() {}

    public ActivityEventDto(String clientEventId, EventType eventType, String filePath, String oldFilePath, Long fileSizeBytes, Instant eventTimestamp) {
        this.clientEventId = clientEventId;
        this.eventType = eventType;
        this.filePath = filePath;
        this.oldFilePath = oldFilePath;
        this.fileSizeBytes = fileSizeBytes;
        this.eventTimestamp = eventTimestamp;
    }

    public String getClientEventId() {
        return clientEventId;
    }

    public void setClientEventId(String clientEventId) {
        this.clientEventId = clientEventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOldFilePath() {
        return oldFilePath;
    }

    public void setOldFilePath(String oldFilePath) {
        this.oldFilePath = oldFilePath;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Instant eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
}
