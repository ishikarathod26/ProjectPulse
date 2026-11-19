package io.projectpulse.backend.blockchain;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
public class HashService {

    public String computeActivityHash(Long organizationId,
                                      Long projectId,
                                      Long userId,
                                      String clientEventId,
                                      String eventType,
                                      String filePath,
                                      Instant eventTimestamp,
                                      Long fileSizeBytes) {
        String canonical = buildCanonicalPayload(
                organizationId, projectId, userId, clientEventId,
                eventType, filePath, eventTimestamp, fileSizeBytes
        );
        return sha256(canonical);
    }

    public String buildCanonicalPayload(Long organizationId,
                                        Long projectId,
                                        Long userId,
                                        String clientEventId,
                                        String eventType,
                                        String filePath,
                                        Instant eventTimestamp,
                                        Long fileSizeBytes) {
        long epochSec = eventTimestamp != null ? eventTimestamp.getEpochSecond() : 0L;
        return String.format(
                "org=%d|proj=%d|user=%d|evtId=%s|type=%s|path=%s|ts=%d|size=%d",
                organizationId != null ? organizationId : 0L,
                projectId != null ? projectId : 0L,
                userId != null ? userId : 0L,
                clientEventId != null ? clientEventId.trim() : "",
                eventType != null ? eventType.trim().toUpperCase() : "",
                filePath != null ? filePath.trim().replace('\\', '/') : "",
                epochSec,
                fileSizeBytes != null ? fileSizeBytes : 0L
        );
    }

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in environment", e);
        }
    }
}
