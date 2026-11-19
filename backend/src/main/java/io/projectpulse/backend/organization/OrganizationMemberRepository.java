package io.projectpulse.backend.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    List<OrganizationMember> findByUserId(Long userId);
    List<OrganizationMember> findByOrganizationId(Long organizationId);
    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);
    long countByOrganizationId(Long organizationId);

    @Query("SELECT om FROM OrganizationMember om JOIN FETCH om.organization WHERE om.user.id = :userId")
    List<OrganizationMember> findAllByUserIdWithOrg(@Param("userId") Long userId);
}
