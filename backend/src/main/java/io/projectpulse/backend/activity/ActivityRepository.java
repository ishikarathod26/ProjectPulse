package io.projectpulse.backend.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    boolean existsByDeviceIdAndClientEventId(Long deviceId, String clientEventId);

    Optional<Activity> findByDeviceIdAndClientEventId(Long deviceId, String clientEventId);

    long countByProjectId(Long projectId);

    long countByOrganizationId(Long organizationId);

    @Query("SELECT a FROM Activity a WHERE a.project.id = :projectId ORDER BY a.eventTimestamp DESC")
    Page<Activity> findByProjectIdOrderByEventTimestampDesc(@Param("projectId") Long projectId, Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE a.organization.id = :orgId ORDER BY a.eventTimestamp DESC")
    Page<Activity> findByOrganizationIdOrderByEventTimestampDesc(@Param("orgId") Long orgId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.project.id = :projectId AND a.eventTimestamp >= :since")
    long countRecentByProjectId(@Param("projectId") Long projectId, @Param("since") Instant since);

    @Query("SELECT a.eventType, COUNT(a) FROM Activity a WHERE a.project.id = :projectId GROUP BY a.eventType")
    List<Object[]> countActivitiesByEventType(@Param("projectId") Long projectId);
}
