package io.projectpulse.backend.organization;

import io.projectpulse.backend.common.BadRequestException;
import io.projectpulse.backend.common.ResourceNotFoundException;
import io.projectpulse.backend.common.UnauthorizedException;
import io.projectpulse.backend.organization.dto.CreateOrgRequest;
import io.projectpulse.backend.organization.dto.InviteMemberRequest;
import io.projectpulse.backend.organization.dto.OrgMemberResponse;
import io.projectpulse.backend.organization.dto.OrgResponse;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               OrganizationMemberRepository memberRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrgResponse createOrganization(CreateOrgRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String slug = generateSlug(request.getName());
        Organization org = new Organization(request.getName().trim(), slug, request.getDescription(), user);
        org = organizationRepository.save(org);

        OrganizationMember member = new OrganizationMember(org, user, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE);
        memberRepository.save(member);

        return mapToResponse(org, OrgRole.ORGANIZATION_OWNER, 1L);
    }

    @Transactional(readOnly = true)
    public List<OrgResponse> getUserOrganizations(Long userId) {
        List<OrganizationMember> memberships = memberRepository.findAllByUserIdWithOrg(userId);
        return memberships.stream().map(m -> {
            Organization org = m.getOrganization();
            long count = memberRepository.countByOrganizationId(org.getId());
            return mapToResponse(org, m.getRole(), count);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrgResponse getOrganizationById(Long orgId, Long userId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this organization"));

        long count = memberRepository.countByOrganizationId(orgId);
        return mapToResponse(org, member.getRole(), count);
    }

    @Transactional(readOnly = true)
    public List<OrgMemberResponse> getOrganizationMembers(Long orgId, Long userId) {
        // Verify user is in org
        if (!memberRepository.existsByOrganizationIdAndUserId(orgId, userId)) {
            User user = userRepository.findById(userId).orElseThrow();
            if (user.getSystemRole() != SystemRole.PLATFORM_ADMIN) {
                throw new UnauthorizedException("Access denied to organization members");
            }
        }

        return memberRepository.findByOrganizationId(orgId).stream().map(m -> new OrgMemberResponse(
                m.getId(),
                m.getUser().getId(),
                m.getUser().getEmail(),
                m.getUser().getFullName(),
                m.getRole(),
                m.getStatus(),
                m.getJoinedAt()
        )).collect(Collectors.toList());
    }

    @Transactional
    public OrgMemberResponse inviteMember(Long orgId, InviteMemberRequest request, Long requestingUserId) {
        OrganizationMember requester = memberRepository.findByOrganizationIdAndUserId(orgId, requestingUserId)
                .orElseThrow(() -> new UnauthorizedException("You are not authorized to invite members to this organization"));

        if (requester.getRole() != OrgRole.ORGANIZATION_OWNER && requester.getRole() != OrgRole.PROJECT_ADMIN) {
            throw new UnauthorizedException("Only organization owners or project administrators can invite members");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        User targetUser = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseGet(() -> {
                    // Create pending user account
                    User newUser = new User(
                            request.getEmail().trim().toLowerCase(),
                            "", // No password until activated
                            request.getEmail().split("@")[0],
                            SystemRole.MEMBER
                    );
                    return userRepository.save(newUser);
                });

        Optional<OrganizationMember> existing = memberRepository.findByOrganizationIdAndUserId(orgId, targetUser.getId());
        if (existing.isPresent()) {
            throw new BadRequestException("User is already a member of this organization");
        }

        OrganizationMember newMember = new OrganizationMember(
                org,
                targetUser,
                request.getRole(),
                MemberStatus.ACTIVE
        );
        newMember = memberRepository.save(newMember);

        return new OrgMemberResponse(
                newMember.getId(),
                targetUser.getId(),
                targetUser.getEmail(),
                targetUser.getFullName(),
                newMember.getRole(),
                newMember.getStatus(),
                newMember.getJoinedAt()
        );
    }

    @Transactional
    public void removeMember(Long orgId, Long targetUserId, Long requestingUserId) {
        OrganizationMember requester = memberRepository.findByOrganizationIdAndUserId(orgId, requestingUserId)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        if (requester.getRole() != OrgRole.ORGANIZATION_OWNER) {
            throw new UnauthorizedException("Only organization owners can remove members");
        }

        OrganizationMember target = memberRepository.findByOrganizationIdAndUserId(orgId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (target.getRole() == OrgRole.ORGANIZATION_OWNER) {
            throw new BadRequestException("Cannot remove the organization owner");
        }

        memberRepository.delete(target);
    }

    public OrganizationMember requireMembership(Long orgId, Long userId) {
        return memberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of organization ID: " + orgId));
    }

    private OrgResponse mapToResponse(Organization org, OrgRole role, long memberCount) {
        return new OrgResponse(
                org.getId(),
                org.getName(),
                org.getSlug(),
                org.getDescription(),
                org.getOwner().getId(),
                org.getOwner().getFullName(),
                role,
                memberCount,
                org.getCreatedAt()
        );
    }

    private String generateSlug(String baseName) {
        String clean = baseName.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
        if (clean.startsWith("-")) clean = clean.substring(1);
        if (clean.endsWith("-")) clean = clean.substring(0, clean.length() - 1);
        if (clean.isEmpty()) clean = "org";

        String candidate = clean;
        int count = 1;
        while (organizationRepository.existsBySlug(candidate)) {
            candidate = clean + "-" + count;
            count++;
        }
        return candidate;
    }
}
