package io.projectpulse.agent.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.projectpulse.agent.model.TrackedEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class BackendApiClient {

    private final String baseUrl;
    private String authToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendApiClient(String baseUrl, String authToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.authToken = authToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/health"))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> login(String email, String password) throws Exception {
        Map<String, String> payload = Map.of(
                "email", email,
                "password", password
        );
        String json = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(7))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Authentication failed (HTTP " + response.statusCode() + "): " + extractErrorMessage(response.body()));
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode data = root.get("data");
        String token = data.get("token").asText();
        this.authToken = token;

        Long activeOrgId = data.hasNonNull("activeOrganizationId") ? data.get("activeOrganizationId").asLong() : null;
        String activeOrgName = data.hasNonNull("activeOrganizationName") ? data.get("activeOrganizationName").asText() : "";

        return Map.of(
                "token", token,
                "userId", data.get("userId").asLong(),
                "fullName", data.get("fullName").asText(),
                "activeOrgId", activeOrgId != null ? activeOrgId : 0L,
                "activeOrgName", activeOrgName
        );
    }

    public void registerDevice(String deviceIdentifier, String deviceName, String osInfo) {
        if (authToken == null || authToken.isBlank()) return;
        try {
            Map<String, String> payload = Map.of(
                    "deviceIdentifier", deviceIdentifier,
                    "deviceName", deviceName,
                    "osInfo", osInfo != null ? osInfo : System.getProperty("os.name")
            );
            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/devices/register"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .timeout(Duration.ofSeconds(5))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            System.err.println("Notice: Device registration heartbeat skipped: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> fetchProjects(Long organizationId) throws Exception {
        if (organizationId == null || organizationId == 0) return Collections.emptyList();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projects?organizationId=" + organizationId))
                .header("Authorization", "Bearer " + authToken)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return Collections.emptyList();
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode data = root.get("data");
        if (data != null && data.isArray()) {
            return objectMapper.convertValue(data, new TypeReference<List<Map<String, Object>>>() {});
        }
        return Collections.emptyList();
    }

    public boolean uploadActivityBatch(Long projectId, String deviceIdentifier, List<TrackedEvent> events) {
        if (events.isEmpty()) return true;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("projectId", projectId);
            payload.put("deviceIdentifier", deviceIdentifier);
            payload.put("events", events);

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/activities/batch"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Batch upload network error: " + e.getMessage());
            return false;
        }
    }

    private String extractErrorMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("message")) return root.get("message").asText();
        } catch (Exception ignored) {}
        return body;
    }
}
