package io.projectpulse.backend.blockchain;

import io.projectpulse.backend.activity.Activity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blockchain_records", indexes = {
    @Index(name = "idx_bc_record_hash", columnList = "record_hash"),
    @Index(name = "idx_bc_tx_id", columnList = "tx_id")
})
public class BlockchainRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    @Column(name = "record_hash", nullable = false, length = 64)
    private String recordHash;

    @Column(name = "tx_id", length = 128)
    private String txId;

    @Column(name = "channel_name", nullable = false, length = 64)
    private String channelName = "projectpulse-channel";

    @Column(name = "chaincode_name", nullable = false, length = 64)
    private String chaincodeName = "activitytracker";

    @Column(name = "block_number")
    private Long blockNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "anchoring_status", nullable = false, length = 30)
    private AnchoringStatus anchoringStatus = AnchoringStatus.PENDING;

    @Column(name = "anchored_at")
    private Instant anchoredAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public BlockchainRecord() {}

    public BlockchainRecord(Activity activity, String recordHash) {
        this.activity = activity;
        this.recordHash = recordHash;
        this.anchoringStatus = AnchoringStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String getRecordHash() {
        return recordHash;
    }

    public void setRecordHash(String recordHash) {
        this.recordHash = recordHash;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
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

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public AnchoringStatus getAnchoringStatus() {
        return anchoringStatus;
    }

    public void setAnchoringStatus(AnchoringStatus anchoringStatus) {
        this.anchoringStatus = anchoringStatus;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
