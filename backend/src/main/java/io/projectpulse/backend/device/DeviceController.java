package io.projectpulse.backend.device;

import io.projectpulse.backend.auth.CurrentUser;
import io.projectpulse.backend.auth.UserPrincipal;
import io.projectpulse.backend.common.ApiResponse;
import io.projectpulse.backend.device.dto.DeviceRegisterRequest;
import io.projectpulse.backend.device.dto.DeviceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Endpoints for Desktop Tracking Agent registration and heartbeat")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register desktop agent workstation or report heartbeat")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody DeviceRegisterRequest request,
            @CurrentUser UserPrincipal principal) {
        DeviceResponse response = deviceService.registerOrHeartbeatDevice(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Device registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all registered devices for the current user")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getUserDevices(@CurrentUser UserPrincipal principal) {
        List<DeviceResponse> devices = deviceService.getUserDevices(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(devices));
    }
}
