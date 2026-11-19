package io.projectpulse.backend.blockchain;

import io.projectpulse.backend.activity.Activity;
import io.projectpulse.backend.activity.ActivityRepository;
import io.projectpulse.backend.blockchain.dto.BlockchainVerifyResponse;
import io.projectpulse.backend.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class VerificationService {

    private final ActivityRepository activityRepository;
    private final BlockchainRecordRepository recordRepository;
    private final HashService hashService;

    public VerificationService(ActivityRepository activityRepository,
                               BlockchainRecordRepository recordRepository,
                               HashService hashService) {
        this.activityRepository = activityRepository;
        this.recordRepository = recordRepository;
        this.hashService = hashService;
    }

    @Transactional
    public BlockchainVerifyResponse verifyActivityIntegrity(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        // Recalculate deterministic hash from current database state
        String recalculatedHash = hashService.computeActivityHash(
                activity.getOrganization().getId(),
                activity.getProject().getId(),
                activity.getUser().getId(),
                activity.getClientEventId(),
                activity.getEventType().name(),
                activity.getFilePath(),
                activity.getEventTimestamp(),
                activity.getFileSizeBytes()
        );

        Optional<BlockchainRecord> recordOpt = recordRepository.findByActivityId(activityId);

        if (recordOpt.isEmpty()) {
            return new BlockchainVerifyResponse(
                    activityId,
                    "UNAVAILABLE",
                    recalculatedHash,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    Instant.now(),
                    "No blockchain record exists for this activity. Hash anchoring has not yet been triggered."
            );
        }

        BlockchainRecord record = recordOpt.get();

        if (record.getAnchoringStatus() == AnchoringStatus.PENDING) {
            return new BlockchainVerifyResponse(
                    activityId,
                    "PENDING",
                    recalculatedHash,
                    record.getRecordHash(),
                    false,
                    record.getTxId(),
                    record.getChannelName(),
                    record.getChaincodeName(),
                    record.getAnchoredAt(),
                    Instant.now(),
                    "This activity hash is currently queued for anchoring in the next block."
            );
        }

        if (record.getAnchoringStatus() == AnchoringStatus.FAILED) {
            return new BlockchainVerifyResponse(
                    activityId,
                    "UNAVAILABLE",
                    recalculatedHash,
                    record.getRecordHash(),
                    false,
                    record.getTxId(),
                    record.getChannelName(),
                    record.getChaincodeName(),
                    null,
                    Instant.now(),
                    "Anchoring failed: " + record.getErrorMessage()
            );
        }

        boolean matches = recalculatedHash.equalsIgnoreCase(record.getRecordHash());
        String status = matches ? "VERIFIED" : "MISMATCHED";
        String explanation = matches
                ? "INTEGRITY VERIFIED: The recalculation of the SHA-256 cryptographic digest directly matches the immutable anchor. No fields have been altered since commit."
                : "TAMPER DETECTED: The current database record digest differs from the immutable hash committed to the blockchain. The database contents have been modified!";

        if (matches) {
            record.setAnchoringStatus(AnchoringStatus.VERIFIED);
            record.setVerifiedAt(Instant.now());
            recordRepository.save(record);
        }

        return new BlockchainVerifyResponse(
                activityId,
                status,
                recalculatedHash,
                record.getRecordHash(),
                matches,
                record.getTxId(),
                record.getChannelName(),
                record.getChaincodeName(),
                record.getAnchoredAt(),
                Instant.now(),
                explanation
        );
    }
}
