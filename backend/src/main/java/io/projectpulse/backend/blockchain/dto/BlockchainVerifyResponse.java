package io.projectpulse.backend.blockchain.dto;

import java.time.Instant;

public class BlockchainVerifyResponse {
    private Long activityId;
    private String status; // VERIFIED, MISMATCHED, PENDING, UNAVAILABLE
    private String recalculatedHash;
    private String anchoredHash;
    private boolean isTamperFree;
    private String transactionId;
    private String channelName;
    private String chaincodeName;
    private Instant anchoredAt;
    private Instant verifiedAt;
    private String explanation;

    public BlockchainVerifyResponse() {}

    public BlockchainVerifyResponse(Long activityId, String status, String recalculatedHash, String anchoredHash, boolean isTamperFree, String transactionId, String channelName, String chaincodeName, Instant anchoredAt, Instant verifiedAt, String explanation) {
        this.activityId = activityId;
        this.status = status;
        this.recalculatedHash = recalculatedHash;
        this.anchoredHash = anchoredHash;
        this.isTamperFree = isTamperFree;
        this.transactionId = transactionId;
        this.channelName = channelName;
        this.chaincodeName = chaincodeName;
        this.anchoredAt = anchoredAt;
        this.verifiedAt = verifiedAt;
        this.explanation = explanation;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecalculatedHash() {
        return recalculatedHash;
    }

    public void setRecalculatedHash(String recalculatedHash) {
        this.recalculatedHash = recalculatedHash;
    }

    public String getAnchoredHash() {
        return anchoredHash;
    }

    public void setAnchoredHash(String anchoredHash) {
        this.anchoredHash = anchoredHash;
    }

    public boolean isTamperFree() {
        return isTamperFree;
    }

    public void setTamperFree(boolean tamperFree) {
        isTamperFree = tamperFree;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChaincodeName() {
        return chaincodeName;
    }

    public void setChaincodeName(String chaincodeName) {
        this.chaincodeName = chaincodeName;
    }

    public Instant getAnchoredAt() {
        return anchoredAt;
    }

    public void setAnchoredAt(Instant anchoredAt) {
        this.anchoredAt = anchoredAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
