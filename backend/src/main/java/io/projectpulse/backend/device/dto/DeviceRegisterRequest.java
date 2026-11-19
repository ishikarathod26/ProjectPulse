package io.projectpulse.backend.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeviceRegisterRequest {

    @NotBlank(message = "Device identifier is required")
    @Size(max = 100, message = "Device identifier cannot exceed 100 characters")
    private String deviceIdentifier;

    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name cannot exceed 100 characters")
    private String deviceName;

    @Size(max = 100, message = "OS info cannot exceed 100 characters")
    private String osInfo;

    public DeviceRegisterRequest() {}

    public DeviceRegisterRequest(String deviceIdentifier, String deviceName, String osInfo) {
        this.deviceIdentifier = deviceIdentifier;
        this.deviceName = deviceName;
        this.osInfo = osInfo;
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
}
