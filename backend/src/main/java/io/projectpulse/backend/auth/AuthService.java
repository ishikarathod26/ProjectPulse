package io.projectpulse.backend.auth;

import io.projectpulse.backend.auth.dto.AuthResponse;
import io.projectpulse.backend.auth.dto.LoginRequest;
import io.projectpulse.backend.auth.dto.RegisterRequest;
import io.projectpulse.backend.auth.dto.UserProfileResponse;
import io.projectpulse.backend.common.BadRequestException;
import io.projectpulse.backend.common.ResourceNotFoundException;
import io.projectpulse.backend.common.UnauthorizedException;
import io.projectpulse.backend.organization.MemberStatus;
import io.projectpulse.backend.organization.OrgRole;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.organization.OrganizationMember;
import io.projectpulse.backend.organization.OrganizationMemberRepository;
import io.projectpulse.backend.organization.OrganizationRepository;
import io.projectpulse.backend.organization.dto.OrgSummaryDto;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       OrganizationMemberRepository organizationMemberRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with this email address already exists.");
        }

        User user = new User(
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                SystemRole.MEMBER
        );
        user = userRepository.save(user);

        // Create default organization
        String orgName = StringUtils.hasText(request.getOrganizationName()) 
                ? request.getOrganizationName().trim() 
                : request.getFullName().trim() + "'s Team";

        String slug = generateSlug(orgName);
        Organization organization = new Organization(orgName, slug, "Default Workspace", user);
        organization = organizationRepository.save(organization);

        OrganizationMember member = new OrganizationMember(
                organization, user, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE
        );
        organizationMemberRepository.save(member);

        UserPrincipal principal = UserPrincipal.create(user);
        String token = tokenProvider.generateToken(principal);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getSystemRole(),
                organization.getId(),
                organization.getName()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userPrincipal);

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserId(userPrincipal.getId());
        Long activeOrgId = null;
        String activeOrgName = null;

        if (!memberships.isEmpty()) {
            Organization firstOrg = memberships.get(0).getOrganization();
            activeOrgId = firstOrg.getId();
            activeOrgName = firstOrg.getName();
        }

        return new AuthResponse(
                token,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getFullName(),
                userPrincipal.getSystemRole(),
                activeOrgId,
                activeOrgName
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse switchOrganization(Long targetOrgId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        OrganizationMember membership = organizationMemberRepository.findByOrganizationIdAndUserId(targetOrgId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of organization ID: " + targetOrgId));

        Organization org = membership.getOrganization();
        UserPrincipal principal = UserPrincipal.create(user);
        String token = tokenProvider.generateToken(principal);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getSystemRole(),
                org.getId(),
                org.getName()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<OrganizationMember> memberships = organizationMemberRepository.findAllByUserIdWithOrg(userId);
        List<OrgSummaryDto> orgDtos = memberships.stream()
                .map(m -> new OrgSummaryDto(
                        m.getOrganization().getId(),
                        m.getOrganization().getName(),
                        m.getOrganization().getSlug(),
                        m.getRole()
                ))
                .collect(Collectors.toList());

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getSystemRole(),
                user.getCreatedAt(),
                orgDtos
        );
    }

    private String generateSlug(String baseName) {
        String clean = baseName.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
        if (clean.startsWith("-")) clean = clean.substring(1);
        if (clean.endsWith("-")) clean = clean.substring(0, clean.length() - 1);
        if (clean.isEmpty()) clean = "workspace";

        String candidate = clean;
        int count = 1;
        while (organizationRepository.existsBySlug(candidate)) {
            candidate = clean + "-" + count;
            count++;
        }
        return candidate;
    }
}
