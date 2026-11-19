package io.projectpulse.backend.activity.dto;

import io.projectpulse.backend.activity.EventType;

import java.time.Instant;

public class ActivityResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long deviceId;
    private String deviceName;
    private String clientEventId;
    private EventType eventType;
    private String filePath;
    private String oldFilePath;
    private Long fileSizeBytes;
    private Instant eventTimestamp;
    private String payloadHash;
    private String anchoringStatus;
    private String blockchainTxId;
    private Instant createdAt;

    public ActivityResponse() {}

    public ActivityResponse(Long id, Long projectId, String projectName, Long userId, String userFullName, String userEmail, Long deviceId, String deviceName, String clientEventId, EventType eventType, String filePath, String oldFilePath, Long fileSizeBytes, Instant eventTimestamp, String payloadHash, String anchoringStatus, String blockchainTxId, Instant createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.clientEventId = clientEventId;
        this.eventType = eventType;
        this.filePath = filePath;
        this.oldFilePath = oldFilePath;
        this.fileSizeBytes = fileSizeBytes;
        this.eventTimestamp = eventTimestamp;
        this.payloadHash = payloadHash;
        this.anchoringStatus = anchoringStatus;
        this.blockchainTxId = blockchainTxId;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public String getAnchoringStatus() {
        return anchoringStatus;
    }

    public void setAnchoringStatus(String anchoringStatus) {
        this.anchoringStatus = anchoringStatus;
    }

    public String getBlockchainTxId() {
        return blockchainTxId;
    }

    public void setBlockchainTxId(String blockchainTxId) {
        this.blockchainTxId = blockchainTxId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
