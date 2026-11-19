package io.projectpulse.backend.device;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceIdentifier(String deviceIdentifier);
    List<Device> findByUserId(Long userId);
    boolean existsByDeviceIdentifier(String deviceIdentifier);
}
