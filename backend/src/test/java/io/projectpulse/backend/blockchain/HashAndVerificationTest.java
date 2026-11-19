package io.projectpulse.backend.blockchain;

import io.projectpulse.backend.activity.Activity;
import io.projectpulse.backend.activity.ActivityRepository;
import io.projectpulse.backend.activity.EventType;
import io.projectpulse.backend.blockchain.dto.BlockchainVerifyResponse;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.project.Project;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HashAndVerificationTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private BlockchainRecordRepository recordRepository;

    private HashService hashService;
    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        hashService = new HashService();
        verificationService = new VerificationService(activityRepository, recordRepository, hashService);
    }

    @Test
    void testCanonicalHashDeterminism() {
        Instant ts = Instant.parse("2026-10-22T10:00:00Z");

        String hash1 = hashService.computeActivityHash(1L, 2L, 3L, "evt-123", "CREATE", "src/App.java", ts, 1024L);
        String hash2 = hashService.computeActivityHash(1L, 2L, 3L, "evt-123", "CREATE", "src/App.java", ts, 1024L);

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2, "Deterministic canonical serialization must yield identical SHA-256 digests");
    }

    @Test
    void testHashChangesOnFieldModification() {
        Instant ts = Instant.parse("2026-10-22T10:00:00Z");

        String originalHash = hashService.computeActivityHash(1L, 2L, 3L, "evt-123", "CREATE", "src/App.java", ts, 1024L);
        String modifiedHash = hashService.computeActivityHash(1L, 2L, 3L, "evt-123", "MODIFY", "src/App.java", ts, 1024L);

        assertNotEquals(originalHash, modifiedHash, "Changing eventType must produce a completely different SHA-256 hash");
    }

    @Test
    void testVerifyActivityIntegrity_Verified() {
        Instant ts = Instant.parse("2026-10-22T10:00:00Z");
        String hash = hashService.computeActivityHash(1L, 2L, 3L, "evt-123", "CREATE", "src/App.java", ts, 1024L);

        User user = new User("u@example.com", "h", "Name", SystemRole.MEMBER);
        user.setId(3L);
        Organization org = new Organization("Org", "org", "Desc", user);
        org.setId(1L);
        Project project = new Project(org, "Proj", "PRJ", "Desc", user);
        project.setId(2L);

        Activity activity = new Activity(org, project, user, null, "evt-123", EventType.CREATE, "src/App.java", null, 1024L, ts, hash);
        activity.setId(99L);

        BlockchainRecord record = new BlockchainRecord(activity, hash);
        record.setAnchoringStatus(AnchoringStatus.ANCHORED);
        record.setTxId("tx-valid-123");

        when(activityRepository.findById(99L)).thenReturn(Optional.of(activity));
        when(recordRepository.findByActivityId(99L)).thenReturn(Optional.of(record));

        BlockchainVerifyResponse result = verificationService.verifyActivityIntegrity(99L);

        assertEquals("VERIFIED", result.getStatus());
        assertTrue(result.isTamperFree());
        assertEquals(hash, result.getRecalculatedHash());
        assertEquals(hash, result.getAnchoredHash());
    }

    @Test
    void testVerifyActivityIntegrity_TamperDetected() {
        Instant ts = Instant.parse("2026-10-22T10:00:00Z");
        String originalHash = hashService.computeActivityHash(1L, 2L, 3L, "evt-123", "CREATE", "src/App.java", ts, 1024L);

        User user = new User("u@example.com", "h", "Name", SystemRole.MEMBER);
        user.setId(3L);
        Organization org = new Organization("Org", "org", "Desc", user);
        org.setId(1L);
        Project project = new Project(org, "Proj", "PRJ", "Desc", user);
        project.setId(2L);

        // Activity has been tampered with in the database: file path altered to malicious-file.sh
        Activity tamperedActivity = new Activity(org, project, user, null, "evt-123", EventType.CREATE, "malicious-file.sh", null, 1024L, ts, originalHash);
        tamperedActivity.setId(99L);

        BlockchainRecord record = new BlockchainRecord(tamperedActivity, originalHash);
        record.setAnchoringStatus(AnchoringStatus.ANCHORED);

        when(activityRepository.findById(99L)).thenReturn(Optional.of(tamperedActivity));
        when(recordRepository.findByActivityId(99L)).thenReturn(Optional.of(record));

        BlockchainVerifyResponse result = verificationService.verifyActivityIntegrity(99L);

        assertEquals("MISMATCHED", result.getStatus());
        assertFalse(result.isTamperFree());
        assertNotEquals(result.getRecalculatedHash(), result.getAnchoredHash());
    }
}
