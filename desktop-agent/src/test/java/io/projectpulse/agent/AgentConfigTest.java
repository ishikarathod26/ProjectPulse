package io.projectpulse.agent;

import io.projectpulse.agent.model.AgentConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AgentConfigTest {

    @Test
    void defaultConfig_HasExpectedValues() {
        AgentConfig config = new AgentConfig();
        assertEquals("http://localhost:8080", config.getServerUrl());
        assertNotNull(config.getDeviceIdentifier());
        assertTrue(config.getDeviceIdentifier().startsWith("dev-"));
        assertNotNull(config.getDeviceName());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        AgentConfig config = new AgentConfig();
        config.setServerUrl("https://pulse.myorg.io");
        config.setAuthToken("jwt-token-123");
        config.setProjectId(42L);
        config.setMonitoredDirectory("/workspace/project");

        assertEquals("https://pulse.myorg.io", config.getServerUrl());
        assertEquals("jwt-token-123", config.getAuthToken());
        assertEquals(42L, config.getProjectId());
        assertEquals("/workspace/project", config.getMonitoredDirectory());
    }
}
