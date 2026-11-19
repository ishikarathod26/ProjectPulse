package io.projectpulse.backend.collaboration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusUpdateRepository extends JpaRepository<StatusUpdate, Long> {
    List<StatusUpdate> findTop10ByProjectIdOrderByCreatedAtDesc(Long projectId);
    long countByProjectId(Long projectId);
}
