package io.projectpulse.backend.device;

import io.projectpulse.backend.common.ResourceNotFoundException;
import io.projectpulse.backend.device.dto.DeviceRegisterRequest;
import io.projectpulse.backend.device.dto.DeviceResponse;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DeviceResponse registerOrHeartbeatDevice(DeviceRegisterRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Device> existing = deviceRepository.findByDeviceIdentifier(request.getDeviceIdentifier().trim());
        Device device;
        if (existing.isPresent()) {
            device = existing.get();
            device.setDeviceName(request.getDeviceName().trim());
            device.setOsInfo(request.getOsInfo());
            device.setLastSeenAt(Instant.now());
            device.setUser(user);
        } else {
            device = new Device(user, request.getDeviceIdentifier().trim(), request.getDeviceName().trim(), request.getOsInfo());
        }
        device = deviceRepository.save(device);

        return mapToResponse(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getUserDevices(Long userId) {
        return deviceRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DeviceResponse mapToResponse(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getDeviceIdentifier(),
                device.getDeviceName(),
                device.getOsInfo(),
                device.getLastSeenAt(),
                device.getStatus(),
                device.getCreatedAt()
        );
    }
}
