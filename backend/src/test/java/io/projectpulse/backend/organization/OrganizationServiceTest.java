package io.projectpulse.backend.organization;

import io.projectpulse.backend.common.BadRequestException;
import io.projectpulse.backend.common.UnauthorizedException;
import io.projectpulse.backend.organization.dto.CreateOrgRequest;
import io.projectpulse.backend.organization.dto.InviteMemberRequest;
import io.projectpulse.backend.organization.dto.OrgMemberResponse;
import io.projectpulse.backend.organization.dto.OrgResponse;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository,
                memberRepository,
                userRepository
        );
    }

    @Test
    void createOrganization_Success() {
        User creator = new User("owner@example.com", "hash", "Owner", SystemRole.MEMBER);
        creator.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(organizationRepository.existsBySlug(any())).thenReturn(false);

        Organization savedOrg = new Organization("Apex Tech", "apex-tech", "Tech Org", creator);
        savedOrg.setId(10L);
        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);
        when(memberRepository.save(any(OrganizationMember.class))).thenReturn(new OrganizationMember());

        CreateOrgRequest request = new CreateOrgRequest("Apex Tech", "Tech Org");
        OrgResponse res = organizationService.createOrganization(request, 1L);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("Apex Tech", res.getName());
        assertEquals("apex-tech", res.getSlug());
        assertEquals(OrgRole.ORGANIZATION_OWNER, res.getCurrentUserRole());
    }

    @Test
    void getOrganizationById_Unauthorized_ThrowsException() {
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(new Organization()));
        when(memberRepository.findByOrganizationIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> organizationService.getOrganizationById(10L, 2L));
    }

    @Test
    void inviteMember_Success() {
        User owner = new User("owner@example.com", "hash", "Owner", SystemRole.MEMBER);
        owner.setId(1L);

        Organization org = new Organization("Apex Tech", "apex-tech", "Desc", owner);
        org.setId(10L);

        OrganizationMember requester = new OrganizationMember(org, owner, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE);
        when(memberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(requester));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        User target = new User("colleague@example.com", "hash", "Colleague", SystemRole.MEMBER);
        target.setId(5L);
        when(userRepository.findByEmail("colleague@example.com")).thenReturn(Optional.of(target));
        when(memberRepository.findByOrganizationIdAndUserId(10L, 5L)).thenReturn(Optional.empty());

        OrganizationMember newMember = new OrganizationMember(org, target, OrgRole.MEMBER, MemberStatus.ACTIVE);
        newMember.setId(101L);
        when(memberRepository.save(any(OrganizationMember.class))).thenReturn(newMember);

        InviteMemberRequest request = new InviteMemberRequest("colleague@example.com", OrgRole.MEMBER);
        OrgMemberResponse response = organizationService.inviteMember(10L, request, 1L);

        assertNotNull(response);
        assertEquals(5L, response.getUserId());
        assertEquals(OrgRole.MEMBER, response.getRole());
    }

    @Test
    void inviteMember_AlreadyMember_ThrowsBadRequest() {
        User owner = new User("owner@example.com", "hash", "Owner", SystemRole.MEMBER);
        owner.setId(1L);
        Organization org = new Organization("Apex Tech", "apex-tech", "Desc", owner);
        org.setId(10L);

        OrganizationMember requester = new OrganizationMember(org, owner, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE);
        when(memberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(requester));
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        User target = new User("colleague@example.com", "hash", "Colleague", SystemRole.MEMBER);
        target.setId(5L);
        when(userRepository.findByEmail("colleague@example.com")).thenReturn(Optional.of(target));
        when(memberRepository.findByOrganizationIdAndUserId(10L, 5L)).thenReturn(Optional.of(new OrganizationMember()));

        InviteMemberRequest request = new InviteMemberRequest("colleague@example.com", OrgRole.MEMBER);
        assertThrows(BadRequestException.class, () -> organizationService.inviteMember(10L, request, 1L));
    }
}
