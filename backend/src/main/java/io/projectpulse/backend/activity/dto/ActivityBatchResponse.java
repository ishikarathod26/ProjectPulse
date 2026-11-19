package io.projectpulse.backend.activity.dto;

public class ActivityBatchResponse {
    private int acceptedCount;
    private int duplicateCount;
    private int rejectedCount;
    private String message;

    public ActivityBatchResponse() {}

    public ActivityBatchResponse(int acceptedCount, int duplicateCount, int rejectedCount, String message) {
        this.acceptedCount = acceptedCount;
        this.duplicateCount = duplicateCount;
        this.rejectedCount = rejectedCount;
        this.message = message;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
