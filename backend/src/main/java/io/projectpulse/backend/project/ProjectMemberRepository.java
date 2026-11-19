package io.projectpulse.backend.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    List<ProjectMember> findByProjectId(Long projectId);
    List<ProjectMember> findByUserId(Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    long countByProjectId(Long projectId);

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.project p WHERE pm.user.id = :userId AND p.organization.id = :organizationId")
    List<ProjectMember> findByUserAndOrganization(@Param("userId") Long userId, @Param("organizationId") Long organizationId);
}
