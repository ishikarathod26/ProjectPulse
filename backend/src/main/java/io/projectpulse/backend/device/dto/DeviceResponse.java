package io.projectpulse.backend.device.dto;

import java.time.Instant;

public class DeviceResponse {
    private Long id;
    private String deviceIdentifier;
    private String deviceName;
    private String osInfo;
    private Instant lastSeenAt;
    private String status;
    private Instant createdAt;

    public DeviceResponse() {}

    public DeviceResponse(Long id, String deviceIdentifier, String deviceName, String osInfo, Instant lastSeenAt, String status, Instant createdAt) {
        this.id = id;
        this.deviceIdentifier = deviceIdentifier;
        this.deviceName = deviceName;
        this.osInfo = osInfo;
        this.lastSeenAt = lastSeenAt;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOsInfo() {
        return osInfo;
    }

    public void setOsInfo(String osInfo) {
        this.osInfo = osInfo;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
