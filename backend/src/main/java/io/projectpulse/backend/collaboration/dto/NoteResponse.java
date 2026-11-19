package io.projectpulse.backend.collaboration.dto;

import java.time.Instant;

public class NoteResponse {
    private Long id;
    private Long projectId;
    private Long userId;
    private String userFullName;
    private Long activityId;
    private String activityFilePath;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public NoteResponse() {}

    public NoteResponse(Long id, Long projectId, Long userId, String userFullName, Long activityId, String activityFilePath, String title, String content, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.activityId = activityId;
        this.activityFilePath = activityFilePath;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityFilePath() {
        return activityFilePath;
    }

    public void setActivityFilePath(String activityFilePath) {
        this.activityFilePath = activityFilePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
