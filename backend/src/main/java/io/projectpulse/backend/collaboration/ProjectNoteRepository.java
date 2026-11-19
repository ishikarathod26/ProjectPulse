package io.projectpulse.backend.collaboration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectNoteRepository extends JpaRepository<ProjectNote, Long> {
    List<ProjectNote> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ProjectNote> findByActivityIdOrderByCreatedAtDesc(Long activityId);
    long countByProjectId(Long projectId);
}
