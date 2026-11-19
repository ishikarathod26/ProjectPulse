package io.projectpulse.backend.collaboration.dto;

import java.time.Instant;

public class StatusUpdateResponse {
    private Long id;
    private Long projectId;
    private Long userId;
    private String userFullName;
    private String statusLabel;
    private String summary;
    private String details;
    private Instant createdAt;

    public StatusUpdateResponse() {}

    public StatusUpdateResponse(Long id, Long projectId, Long userId, String userFullName, String statusLabel, String summary, String details, Instant createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.statusLabel = statusLabel;
        this.summary = summary;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
