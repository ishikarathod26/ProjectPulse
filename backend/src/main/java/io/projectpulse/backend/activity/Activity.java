package io.projectpulse.backend.activity;

import io.projectpulse.backend.device.Device;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.project.Project;
import io.projectpulse.backend.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "activities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"device_id", "client_event_id"})
}, indexes = {
    @Index(name = "idx_act_org_proj_ts", columnList = "organization_id, project_id, event_timestamp"),
    @Index(name = "idx_act_payload_hash", columnList = "payload_hash")
})
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "client_event_id", nullable = false, length = 100)
    private String clientEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "old_file_path", length = 500)
    private String oldFilePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Activity() {}

    public Activity(Organization organization, Project project, User user, Device device,
                    String clientEventId, EventType eventType, String filePath, String oldFilePath,
                    Long fileSizeBytes, Instant eventTimestamp, String payloadHash) {
        this.organization = organization;
        this.project = project;
        this.user = user;
        this.device = device;
        this.clientEventId = clientEventId;
        this.eventType = eventType;
        this.filePath = filePath;
        this.oldFilePath = oldFilePath;
        this.fileSizeBytes = fileSizeBytes;
        this.eventTimestamp = eventTimestamp;
        this.payloadHash = payloadHash;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
