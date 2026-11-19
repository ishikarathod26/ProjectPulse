package io.projectpulse.backend.collaboration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop50ByProjectIdOrderByCreatedAtDesc(Long projectId);
    Page<Message> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
    long countByProjectId(Long projectId);
}
