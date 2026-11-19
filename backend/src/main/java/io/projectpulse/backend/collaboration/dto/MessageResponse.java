package io.projectpulse.backend.collaboration.dto;

import java.time.Instant;

public class MessageResponse {
    private Long id;
    private Long projectId;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String content;
    private Instant createdAt;

    public MessageResponse() {}

    public MessageResponse(Long id, Long projectId, Long userId, String userFullName, String userEmail, String content, Instant createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.content = content;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
}
