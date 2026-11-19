package io.projectpulse.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.projectpulse.backend.activity.dto.ActivityBatchRequest;
import io.projectpulse.backend.activity.dto.ActivityEventDto;
import io.projectpulse.backend.auth.dto.LoginRequest;
import io.projectpulse.backend.auth.dto.RegisterRequest;
import io.projectpulse.backend.collaboration.dto.CreateNoteRequest;
import io.projectpulse.backend.collaboration.dto.CreateStatusUpdateRequest;
import io.projectpulse.backend.collaboration.dto.SendMessageRequest;
import io.projectpulse.backend.device.dto.DeviceRegisterRequest;
import io.projectpulse.backend.organization.dto.CreateOrgRequest;
import io.projectpulse.backend.project.dto.CreateProjectRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectPulseE2EIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String userToken;
    private static Long userId;
    private static Long orgId;
    private static Long projectId;
    private static Long activityId;

    private static String otherUserToken;
    private static Long otherOrgId;

    @Test
    @Order(1)
    @DisplayName("1. Health Endpoint returns UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("ProjectPulse API"));
    }

    @Test
    @Order(2)
    @DisplayName("2. User Registration creates user and default organization")
    void testUserRegistration() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "jane.integration@example.com",
                "Password123!",
                "Jane Integration",
                "Integration Org"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.activeOrganizationId").isNumber())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        userToken = root.get("data").get("token").asText();
        userId = root.get("data").get("userId").asLong();
        orgId = root.get("data").get("activeOrganizationId").asLong();

        assertNotNull(userToken);
        assertNotNull(userId);
        assertNotNull(orgId);
    }

    @Test
    @Order(3)
    @DisplayName("3. User Login issues valid JWT session")
    void testUserLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("jane.integration@example.com", "Password123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jane.integration@example.com"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        userToken = root.get("data").get("token").asText();
        assertNotNull(userToken);
    }

    @Test
    @Order(4)
    @DisplayName("4. Authenticated Profile retrieves organizations and active org")
    void testUserProfile() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jane.integration@example.com"))
                .andExpect(jsonPath("$.data.organizations").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("5. Create second Organization and Switch Active Workspace")
    void testOrganizationCreateAndSwitch() throws Exception {
        CreateOrgRequest createOrgRequest = new CreateOrgRequest("Secondary Workspace", "Testing Org Switching");

        MvcResult createResult = mockMvc.perform(post("/api/organizations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrgRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Secondary Workspace"))
                .andReturn();

        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long secondOrgId = createdNode.get("data").get("id").asLong();

        // Switch to second org
        MvcResult switchResult = mockMvc.perform(post("/api/auth/switch-org/" + secondOrgId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeOrganizationId").value(secondOrgId))
                .andReturn();

        JsonNode switchedNode = objectMapper.readTree(switchResult.getResponse().getContentAsString());
        userToken = switchedNode.get("data").get("token").asText();

        // Switch back to primary org
        MvcResult switchBack = mockMvc.perform(post("/api/auth/switch-org/" + orgId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeOrganizationId").value(orgId))
                .andReturn();

        JsonNode backNode = objectMapper.readTree(switchBack.getResponse().getContentAsString());
        userToken = backNode.get("data").get("token").asText();
    }

    @Test
    @Order(6)
    @DisplayName("6. Project CRUD operations within organization")
    void testProjectCrud() throws Exception {
        CreateProjectRequest projectRequest = new CreateProjectRequest(
                orgId,
                "Project Alpha",
                "ALPHA",
                "Alpha testing project workspace"
        );

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyIdentifier").value("ALPHA"))
                .andExpect(jsonPath("$.data.name").value("Project Alpha"))
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        projectId = node.get("data").get("id").asLong();
        assertNotNull(projectId);

        // List projects in organization
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + userToken)
                        .param("organizationId", orgId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Get single project
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(projectId));
    }

    @Test
    @Order(7)
    @DisplayName("7. Multi-Tenant Cross-Organization Data Isolation")
    void testMultiTenantIsolation() throws Exception {
        // Register a distinct external user in a different organization
        RegisterRequest hackerRequest = new RegisterRequest(
                "external.tenant@othercorp.com",
                "Password123!",
                "External User",
                "Other Corporation"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hackerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        otherUserToken = node.get("data").get("token").asText();
        otherOrgId = node.get("data").get("activeOrganizationId").asLong();

        // Attempt to access Project Alpha belonging to orgId using otherUserToken -> Must fail
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    @DisplayName("8. Device Registration & Heartbeat")
    void testDeviceRegistration() throws Exception {
        DeviceRegisterRequest deviceRequest = new DeviceRegisterRequest(
                "device-agent-01",
                "E2E-Desktop-Agent",
                "Windows 11 Java 21"
        );

        mockMvc.perform(post("/api/devices/register")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deviceIdentifier").value("device-agent-01"));

        // List devices
        mockMvc.perform(get("/api/devices")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].deviceIdentifier").value("device-agent-01"));
    }

    @Test
    @Order(9)
    @DisplayName("9. Batch Activity Ingestion & Idempotency / Duplicate Prevention")
    void testActivityBatchIngestionAndDeduplication() throws Exception {
        String event1 = UUID.randomUUID().toString();
        String event2 = UUID.randomUUID().toString();
        Instant now = Instant.now();

        ActivityEventDto e1 = new ActivityEventDto(event1, io.projectpulse.backend.activity.EventType.CREATE, "src/index.js", null, 1024L, now);
        ActivityEventDto e2 = new ActivityEventDto(event2, io.projectpulse.backend.activity.EventType.MODIFY, "src/index.js", null, 1450L, now);

        ActivityBatchRequest batchRequest = new ActivityBatchRequest(
                projectId,
                "device-agent-01",
                List.of(e1, e2)
        );

        // First upload: Both events should be processed
        mockMvc.perform(post("/api/activities/batch")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acceptedCount").value(2));

        // Re-upload the exact same batch: Deduplication should prevent duplicate persistence
        mockMvc.perform(post("/api/activities/batch")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acceptedCount").value(0))
                .andExpect(jsonPath("$.data.duplicateCount").value(2));
    }

    @Test
    @Order(10)
    @DisplayName("10. Path Traversal Defense rejects invalid file paths")
    void testPathTraversalDefense() throws Exception {
        ActivityEventDto evilEvent = new ActivityEventDto(
                UUID.randomUUID().toString(),
                io.projectpulse.backend.activity.EventType.CREATE,
                "../../etc/shadow",
                null,
                512L,
                Instant.now()
        );

        ActivityBatchRequest badBatch = new ActivityBatchRequest(
                projectId,
                "device-agent-01",
                List.of(evilEvent)
        );

        mockMvc.perform(post("/api/activities/batch")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badBatch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rejectedCount").value(1));
    }

    @Test
    @Order(11)
    @DisplayName("11. Activity Query, Pagination, and Deterministic Hash Verification")
    void testActivityQueryAndHashing() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/activities")
                        .header("Authorization", "Bearer " + userToken)
                        .param("projectId", projectId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].payloadHash").isNotEmpty())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        activityId = root.get("data").get("content").get(0).get("id").asLong();
        assertNotNull(activityId);
    }

    @Test
    @Order(12)
    @DisplayName("12. Honest Blockchain Verification reporting")
    void testBlockchainVerification() throws Exception {
        // Status of Fabric gateway
        mockMvc.perform(get("/api/blockchain/status")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.networkType").value("Hyperledger Fabric 2.5"));

        // Verification endpoint returns VERIFIED when record matches anchored hash
        mockMvc.perform(get("/api/blockchain/verify/" + activityId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.tamperFree").value(true))
                .andExpect(jsonPath("$.data.activityId").value(activityId))
                .andExpect(jsonPath("$.data.recalculatedHash").isNotEmpty());
    }

    @Test
    @Order(13)
    @DisplayName("13. Collaboration: Messages, Activity-Linked Notes, Status Updates")
    void testCollaborationSuite() throws Exception {
        // Send message
        SendMessageRequest msgRequest = new SendMessageRequest("All integration checks passing!");
        mockMvc.perform(post("/api/projects/" + projectId + "/messages")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("All integration checks passing!"));

        // Create Note linked to activity
        CreateNoteRequest noteRequest = new CreateNoteRequest(
                "Code Review Verified",
                "Automated E2E pipeline verified file structure and integrity.",
                activityId
        );
        mockMvc.perform(post("/api/projects/" + projectId + "/notes")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activityId").value(activityId));

        // Create Status Update
        CreateStatusUpdateRequest statusRequest = new CreateStatusUpdateRequest(
                "HEALTHY",
                "Sprint Milestone Completed",
                "Full stack end-to-end integration verified successfully."
        );
        mockMvc.perform(post("/api/projects/" + projectId + "/status-updates")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusLabel").value("HEALTHY"));
    }

    @Test
    @Order(14)
    @DisplayName("14. Platform Administration: Stats & Audit Logs")
    void testPlatformAdminSuite() throws Exception {
        // Login as initial platform admin
       LoginRequest adminLogin = new LoginRequest("admin@projectpulse.io", "TestOnlyPassword123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String adminToken = root.get("data").get("token").asText();

        // Platform stats
        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.totalProjects").isNumber());

        // Audit logs
        mockMvc.perform(get("/api/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
