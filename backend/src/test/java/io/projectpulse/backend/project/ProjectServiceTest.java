package io.projectpulse.backend.project;

import io.projectpulse.backend.common.BadRequestException;
import io.projectpulse.backend.common.UnauthorizedException;
import io.projectpulse.backend.organization.MemberStatus;
import io.projectpulse.backend.organization.OrgRole;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.organization.OrganizationMember;
import io.projectpulse.backend.organization.OrganizationMemberRepository;
import io.projectpulse.backend.organization.OrganizationRepository;
import io.projectpulse.backend.project.dto.AddProjectMemberRequest;
import io.projectpulse.backend.project.dto.CreateProjectRequest;
import io.projectpulse.backend.project.dto.ProjectMemberResponse;
import io.projectpulse.backend.project.dto.ProjectResponse;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository orgMemberRepository;

    @Mock
    private UserRepository userRepository;

    private ProjectService projectService;

    private User owner;
    private Organization org;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(
                projectRepository,
                projectMemberRepository,
                organizationRepository,
                orgMemberRepository,
                userRepository
        );

        owner = new User("owner@example.com", "hash", "Owner", SystemRole.MEMBER);
        owner.setId(1L);

        org = new Organization("Tech Corp", "tech-corp", "Description", owner);
        org.setId(10L);
    }

    @Test
    void createProject_Success() {
        CreateProjectRequest request = new CreateProjectRequest(10L, "New App", "APP", "App description");

        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        OrganizationMember membership = new OrganizationMember(org, owner, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE);
        when(orgMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
        when(projectRepository.existsByOrganizationIdAndKeyIdentifier(10L, "APP")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        Project savedProject = new Project(org, "New App", "APP", "App description", owner);
        savedProject.setId(100L);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(new ProjectMember());

        ProjectResponse response = projectService.createProject(request, 1L);

        assertNotNull(response);
        assertEquals("New App", response.getName());
        assertEquals("APP", response.getKeyIdentifier());
        assertEquals(ProjectRole.PROJECT_ADMIN, response.getCurrentUserRole());
    }

    @Test
    void createProject_UnauthorizedUser_ThrowsUnauthorizedException() {
        CreateProjectRequest request = new CreateProjectRequest(10L, "App", "APP", "Desc");
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        // Member is a read-only VIEWER in organization
        OrganizationMember viewerMember = new OrganizationMember(org, owner, OrgRole.VIEWER, MemberStatus.ACTIVE);
        when(orgMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(viewerMember));

        assertThrows(UnauthorizedException.class, () -> projectService.createProject(request, 1L));
    }

    @Test
    void createProject_DuplicateKey_ThrowsBadRequestException() {
        CreateProjectRequest request = new CreateProjectRequest(10L, "App", "APP", "Desc");
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        OrganizationMember membership = new OrganizationMember(org, owner, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE);
        when(orgMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
        when(projectRepository.existsByOrganizationIdAndKeyIdentifier(10L, "APP")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> projectService.createProject(request, 1L));
    }

    @Test
    void addProjectMember_TargetNotInOrg_ThrowsBadRequestException() {
        Project project = new Project(org, "App", "APP", "Desc", owner);
        project.setId(100L);
        when(projectRepository.findById(100L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        OrganizationMember ownerMember = new OrganizationMember(org, owner, OrgRole.ORGANIZATION_OWNER, MemberStatus.ACTIVE);
        when(orgMemberRepository.findByOrganizationIdAndUserId(10L, 1L)).thenReturn(Optional.of(ownerMember));

        // User 2 is not in org
        when(orgMemberRepository.existsByOrganizationIdAndUserId(10L, 2L)).thenReturn(false);

        AddProjectMemberRequest req = new AddProjectMemberRequest(2L, ProjectRole.MEMBER);
        assertThrows(BadRequestException.class, () -> projectService.addProjectMember(100L, req, 1L));
    }
}
