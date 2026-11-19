package io.projectpulse.backend.collaboration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateStatusUpdateRequest {

    @NotBlank(message = "Status label is required (e.g., ON_TRACK, AT_RISK, BLOCKED, COMPLETED)")
    @Size(max = 50, message = "Status label cannot exceed 50 characters")
    private String statusLabel;

    @NotBlank(message = "Summary is required")
    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    private String summary;

    @Size(max = 5000, message = "Details cannot exceed 5000 characters")
    private String details;

    public CreateStatusUpdateRequest() {}

    public CreateStatusUpdateRequest(String statusLabel, String summary, String details) {
        this.statusLabel = statusLabel;
        this.summary = summary;
        this.details = details;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
