package io.projectpulse.backend.blockchain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecord, Long> {
    Optional<BlockchainRecord> findByActivityId(Long activityId);
    Optional<BlockchainRecord> findByRecordHash(String recordHash);
    long countByAnchoringStatus(AnchoringStatus status);
}
