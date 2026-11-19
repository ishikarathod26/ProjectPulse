package io.projectpulse.agent.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class TrackedEvent {
    private String clientEventId;
    private String eventType; // CREATE, MODIFY, DELETE, RENAME
    private String filePath;
    private String oldFilePath;
    private Long fileSizeBytes;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant eventTimestamp;

    public TrackedEvent() {}

    public TrackedEvent(String clientEventId, String eventType, String filePath, String oldFilePath, Long fileSizeBytes, Instant eventTimestamp) {
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
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

    @Override
    public String toString() {
        return String.format("[%s] %s (%s bytes) at %s", eventType, filePath, fileSizeBytes != null ? fileSizeBytes : 0, eventTimestamp);
    }
}
