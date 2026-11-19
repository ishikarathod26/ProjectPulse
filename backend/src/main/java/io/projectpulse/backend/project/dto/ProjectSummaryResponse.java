package io.projectpulse.backend.project.dto;

import io.projectpulse.backend.project.ProjectStatus;

public class ProjectSummaryResponse {
    private Long id;
    private String name;
    private String keyIdentifier;
    private ProjectStatus status;
    private long totalActivities;
    private long totalMembers;
    private long totalNotes;
    private long totalMessages;

    public ProjectSummaryResponse() {}

    public ProjectSummaryResponse(Long id, String name, String keyIdentifier, ProjectStatus status, long totalActivities, long totalMembers, long totalNotes, long totalMessages) {
        this.id = id;
        this.name = name;
        this.keyIdentifier = keyIdentifier;
        this.status = status;
        this.totalActivities = totalActivities;
        this.totalMembers = totalMembers;
        this.totalNotes = totalNotes;
        this.totalMessages = totalMessages;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyIdentifier() {
        return keyIdentifier;
    }

    public void setKeyIdentifier(String keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public long getTotalNotes() {
        return totalNotes;
    }

    public void setTotalNotes(long totalNotes) {
        this.totalNotes = totalNotes;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }
}
