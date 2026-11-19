package io.projectpulse.backend.project;

import io.projectpulse.backend.common.BadRequestException;
import io.projectpulse.backend.common.ResourceNotFoundException;
import io.projectpulse.backend.common.UnauthorizedException;
import io.projectpulse.backend.organization.OrgRole;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.organization.OrganizationMember;
import io.projectpulse.backend.organization.OrganizationMemberRepository;
import io.projectpulse.backend.organization.OrganizationRepository;
import io.projectpulse.backend.project.dto.*;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository orgMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          OrganizationRepository organizationRepository,
                          OrganizationMemberRepository orgMemberRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.organizationRepository = organizationRepository;
        this.orgMemberRepository = orgMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Long userId) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        OrganizationMember orgMember = orgMemberRepository.findByOrganizationIdAndUserId(org.getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this organization"));

        if (orgMember.getRole() != OrgRole.ORGANIZATION_OWNER && orgMember.getRole() != OrgRole.PROJECT_ADMIN) {
            throw new UnauthorizedException("Only organization owners or project administrators can create projects");
        }

        String key = request.getKeyIdentifier().trim().toUpperCase();
        if (projectRepository.existsByOrganizationIdAndKeyIdentifier(org.getId(), key)) {
            throw new BadRequestException("A project with key '" + key + "' already exists in this organization");
        }

        User creator = userRepository.findById(userId).orElseThrow();
        Project project = new Project(org, request.getName().trim(), key, request.getDescription(), creator);
        project = projectRepository.save(project);

        // Add creator as PROJECT_ADMIN in project_members
        ProjectMember member = new ProjectMember(project, creator, ProjectRole.PROJECT_ADMIN);
        projectMemberRepository.save(member);

        return mapToResponse(project, ProjectRole.PROJECT_ADMIN, 1L);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getOrganizationProjects(Long orgId, Long userId) {
        // Ensure user belongs to organization
        if (!orgMemberRepository.existsByOrganizationIdAndUserId(orgId, userId)) {
            User user = userRepository.findById(userId).orElseThrow();
            if (user.getSystemRole() != SystemRole.PLATFORM_ADMIN) {
                throw new UnauthorizedException("You do not belong to this organization");
            }
        }

        List<Project> projects = projectRepository.findByOrganizationId(orgId);
        return projects.stream().map(p -> {
            Optional<ProjectMember> pm = projectMemberRepository.findByProjectIdAndUserId(p.getId(), userId);
            ProjectRole role = pm.map(ProjectMember::getRole).orElse(null);
            long memberCount = projectMemberRepository.countByProjectId(p.getId());
            return mapToResponse(p, role, memberCount);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        validateProjectAccess(project, userId);

        Optional<ProjectMember> pm = projectMemberRepository.findByProjectIdAndUserId(projectId, userId);
        ProjectRole role = pm.map(ProjectMember::getRole).orElse(null);
        long memberCount = projectMemberRepository.countByProjectId(projectId);

        return mapToResponse(project, role, memberCount);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectAdminAccess(project, userId);

        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        project = projectRepository.save(project);
        long memberCount = projectMemberRepository.countByProjectId(projectId);
        return mapToResponse(project, ProjectRole.PROJECT_ADMIN, memberCount);
    }

    @Transactional
    public void archiveProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectAdminAccess(project, userId);
        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectAccess(project, userId);

        return projectMemberRepository.findByProjectId(projectId).stream().map(pm -> new ProjectMemberResponse(
                pm.getId(),
                pm.getUser().getId(),
                pm.getUser().getEmail(),
                pm.getUser().getFullName(),
                pm.getRole(),
                pm.getAddedAt()
        )).collect(Collectors.toList());
    }

    @Transactional
    public ProjectMemberResponse addProjectMember(Long projectId, AddProjectMemberRequest request, Long requestingUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectAdminAccess(project, requestingUserId);

        // Target user must belong to parent organization
        if (!orgMemberRepository.existsByOrganizationIdAndUserId(project.getOrganization().getId(), request.getUserId())) {
            throw new BadRequestException("Target user must be a member of the organization before joining the project");
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new BadRequestException("User is already a member of this project");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectMember member = new ProjectMember(project, user, request.getRole());
        member = projectMemberRepository.save(member);

        return new ProjectMemberResponse(
                member.getId(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                member.getRole(),
                member.getAddedAt()
        );
    }

    @Transactional
    public void removeProjectMember(Long projectId, Long targetUserId, Long requestingUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProjectAdminAccess(project, requestingUserId);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found"));

        projectMemberRepository.delete(member);
    }

    public void validateProjectAccess(Project project, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getSystemRole() == SystemRole.PLATFORM_ADMIN) {
            return;
        }

        // Check if user is Org Owner/Admin
        Optional<OrganizationMember> om = orgMemberRepository.findByOrganizationIdAndUserId(project.getOrganization().getId(), userId);
        if (om.isPresent() && (om.get().getRole() == OrgRole.ORGANIZATION_OWNER || om.get().getRole() == OrgRole.PROJECT_ADMIN)) {
            return;
        }

        // Check if user is a member of the project
        if (!projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userId)) {
            throw new UnauthorizedException("You do not have permission to access project: " + project.getName());
        }
    }

    public void validateProjectAdminAccess(Project project, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getSystemRole() == SystemRole.PLATFORM_ADMIN) {
            return;
        }

        Optional<OrganizationMember> om = orgMemberRepository.findByOrganizationIdAndUserId(project.getOrganization().getId(), userId);
        if (om.isPresent() && om.get().getRole() == OrgRole.ORGANIZATION_OWNER) {
            return;
        }

        Optional<ProjectMember> pm = projectMemberRepository.findByProjectIdAndUserId(project.getId(), userId);
        if (pm.isEmpty() || pm.get().getRole() != ProjectRole.PROJECT_ADMIN) {
            throw new UnauthorizedException("Project administrator privileges required");
        }
    }

    private ProjectResponse mapToResponse(Project project, ProjectRole role, long memberCount) {
        return new ProjectResponse(
                project.getId(),
                project.getOrganization().getId(),
                project.getOrganization().getName(),
                project.getName(),
                project.getKeyIdentifier(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedBy().getId(),
                project.getCreatedBy().getFullName(),
                role,
                memberCount,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
