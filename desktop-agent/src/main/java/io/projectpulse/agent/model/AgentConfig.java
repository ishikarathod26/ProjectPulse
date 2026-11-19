package io.projectpulse.agent.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;

public class AgentConfig {
    private String serverUrl = "http://localhost:8080";
    private String authToken = "";
    private Long projectId = 1L;
    private String deviceIdentifier = "";
    private String deviceName = "";
    private String monitoredDirectory = "";

    private static final String CONFIG_FILE = "projectpulse-agent.properties";

    public AgentConfig() {
        // Generate or load unique machine identifier
        this.deviceIdentifier = "dev-" + UUID.randomUUID().toString().substring(0, 8);
        this.deviceName = resolveDefaultDeviceName();
    }

    public static AgentConfig load() {
        AgentConfig config = new AgentConfig();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(in);
                config.serverUrl = props.getProperty("serverUrl", config.serverUrl);
                config.authToken = props.getProperty("authToken", config.authToken);
                config.projectId = Long.parseLong(props.getProperty("projectId", "1"));
                config.deviceIdentifier = props.getProperty("deviceIdentifier", config.deviceIdentifier);
                config.deviceName = props.getProperty("deviceName", config.deviceName);
                config.monitoredDirectory = props.getProperty("monitoredDirectory", config.monitoredDirectory);
            } catch (Exception e) {
                System.err.println("Notice: Could not load existing config file: " + e.getMessage());
            }
        }
        return config;
    }

    public void save() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("serverUrl", serverUrl != null ? serverUrl : "http://localhost:8080");
            props.setProperty("authToken", authToken != null ? authToken : "");
            props.setProperty("projectId", String.valueOf(projectId != null ? projectId : 1L));
            props.setProperty("deviceIdentifier", deviceIdentifier);
            props.setProperty("deviceName", deviceName);
            props.setProperty("monitoredDirectory", monitoredDirectory != null ? monitoredDirectory : "");
            props.store(out, "ProjectPulse Desktop Agent Configuration");
        } catch (Exception e) {
            System.err.println("Notice: Could not persist config file: " + e.getMessage());
        }
    }

    private static String resolveDefaultDeviceName() {
        String host = System.getenv("COMPUTERNAME");
        if (host == null || host.isBlank()) {
            host = System.getenv("HOSTNAME");
        }
        if (host == null || host.isBlank()) {
            host = "Workstation-" + System.getProperty("user.name", "user");
        }
        return host;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMonitoredDirectory() {
        return monitoredDirectory;
    }

    public void setMonitoredDirectory(String monitoredDirectory) {
        this.monitoredDirectory = monitoredDirectory;
    }
}
