package io.projectpulse.backend.auth;

import io.projectpulse.backend.auth.dto.AuthResponse;
import io.projectpulse.backend.auth.dto.LoginRequest;
import io.projectpulse.backend.auth.dto.RegisterRequest;
import io.projectpulse.backend.auth.dto.UserProfileResponse;
import io.projectpulse.backend.common.BadRequestException;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.organization.OrganizationMember;
import io.projectpulse.backend.organization.OrganizationMemberRepository;
import io.projectpulse.backend.organization.OrganizationRepository;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                organizationRepository,
                organizationMemberRepository,
                passwordEncoder,
                authenticationManager,
                tokenProvider
        );
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("alice@example.com", "Password123!", "Alice Smith", "Acme Labs");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-pwd");

        User savedUser = new User("alice@example.com", "encoded-pwd", "Alice Smith", SystemRole.MEMBER);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Organization savedOrg = new Organization("Acme Labs", "acme-labs", "Default Workspace", savedUser);
        savedOrg.setId(20L);
        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);
        when(organizationMemberRepository.save(any(OrganizationMember.class))).thenReturn(new OrganizationMember());
        when(tokenProvider.generateToken(any())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals(10L, response.getUserId());
        assertEquals(20L, response.getActiveOrganizationId());
    }

    @Test
    void register_DuplicateEmail_ThrowsBadRequestException() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "Password123!", "Alice", "Acme");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("bob@example.com", "SecurePass1!");
        User user = new User("bob@example.com", "hashed", "Bob Jones", SystemRole.MEMBER);
        user.setId(5L);
        UserPrincipal principal = UserPrincipal.create(user);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(tokenProvider.generateToken(principal)).thenReturn("bob-jwt-token");
        when(organizationMemberRepository.findByUserId(5L)).thenReturn(Collections.emptyList());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("bob-jwt-token", response.getToken());
        assertEquals(5L, response.getUserId());
        assertEquals("bob@example.com", response.getEmail());
    }

    @Test
    void getCurrentUserProfile_Success() {
        User user = new User("user@example.com", "hash", "John Doe", SystemRole.MEMBER);
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(organizationMemberRepository.findAllByUserIdWithOrg(7L)).thenReturn(Collections.emptyList());

        UserProfileResponse profile = authService.getCurrentUserProfile(7L);

        assertNotNull(profile);
        assertEquals(7L, profile.getId());
        assertEquals("John Doe", profile.getFullName());
        assertEquals("user@example.com", profile.getEmail());
    }

    @Test
    void switchOrganization_Success() {
        User user = new User("user@example.com", "hash", "John Doe", SystemRole.MEMBER);
        user.setId(7L);
        Organization org = new Organization("New Org", "new-org", "Description", user);
        org.setId(99L);
        OrganizationMember membership = new OrganizationMember(org, user, io.projectpulse.backend.organization.OrgRole.MEMBER, io.projectpulse.backend.organization.MemberStatus.ACTIVE);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(99L, 7L)).thenReturn(Optional.of(membership));
        when(tokenProvider.generateToken(any())).thenReturn("switched-token");

        AuthResponse res = authService.switchOrganization(99L, 7L);

        assertNotNull(res);
        assertEquals(99L, res.getActiveOrganizationId());
        assertEquals("New Org", res.getActiveOrganizationName());
        assertEquals("switched-token", res.getToken());
    }

    @Test
    void switchOrganization_Unauthorized_ThrowsException() {
        User user = new User("user@example.com", "hash", "John Doe", SystemRole.MEMBER);
        user.setId(7L);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(organizationMemberRepository.findByOrganizationIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThrows(io.projectpulse.backend.common.UnauthorizedException.class, () -> authService.switchOrganization(99L, 7L));
    }
}
