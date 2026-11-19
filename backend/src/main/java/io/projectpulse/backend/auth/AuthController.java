package io.projectpulse.backend.auth;

import io.projectpulse.backend.auth.dto.AuthResponse;
import io.projectpulse.backend.auth.dto.LoginRequest;
import io.projectpulse.backend.auth.dto.RegisterRequest;
import io.projectpulse.backend.auth.dto.UserProfileResponse;
import io.projectpulse.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and session management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Register a new user account and initialize their workspace")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully", response));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Authenticate user and issue JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authenticated successfully", response));
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Retrieve current authenticated user profile and organizations")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(@CurrentUser UserPrincipal principal) {
        if (principal == null) {
            throw new io.projectpulse.backend.common.UnauthorizedException("User must be authenticated");
        }
        UserProfileResponse profile = authService.getCurrentUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PostMapping("/auth/switch-org/{orgId}")
    @Operation(summary = "Switch active organization workspace")
    public ResponseEntity<ApiResponse<AuthResponse>> switchOrganization(
            @PathVariable Long orgId,
            @CurrentUser UserPrincipal principal) {
        if (principal == null) {
            throw new io.projectpulse.backend.common.UnauthorizedException("User must be authenticated");
        }
        AuthResponse response = authService.switchOrganization(orgId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Switched to organization: " + response.getActiveOrganizationName(), response));
    }

    @GetMapping("/health")
    @Operation(summary = "System health check endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "status", "UP",
                "service", "ProjectPulse API",
                "version", "1.0.0"
        )));
    }
}
