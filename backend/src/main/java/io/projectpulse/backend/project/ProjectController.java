package io.projectpulse.backend.project;

import io.projectpulse.backend.auth.CurrentUser;
import io.projectpulse.backend.auth.UserPrincipal;
import io.projectpulse.backend.common.ApiResponse;
import io.projectpulse.backend.project.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Endpoints for creating, managing, and configuring project workspaces and permissions")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create a new project inside an organization")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @CurrentUser UserPrincipal principal) {
        ProjectResponse response = projectService.createProject(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all projects in an organization")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getOrganizationProjects(
            @RequestParam Long organizationId,
            @CurrentUser UserPrincipal principal) {
        List<ProjectResponse> projects = projectService.getOrganizationProjects(organizationId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable Long projectId,
            @CurrentUser UserPrincipal principal) {
        ProjectResponse project = projectService.getProjectById(projectId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update project metadata or status")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @CurrentUser UserPrincipal principal) {
        ProjectResponse project = projectService.updateProject(projectId, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @RequestMapping(value = "/{projectId}/archive", method = {RequestMethod.POST, RequestMethod.PATCH})
    @Operation(summary = "Archive a project")
    public ResponseEntity<ApiResponse<Void>> archiveProject(
            @PathVariable Long projectId,
            @CurrentUser UserPrincipal principal) {
        projectService.archiveProject(projectId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Project archived successfully", null));
    }

    @GetMapping("/{projectId}/members")
    @Operation(summary = "List all members of a project")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Long projectId,
            @CurrentUser UserPrincipal principal) {
        List<ProjectMemberResponse> members = projectService.getProjectMembers(projectId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping("/{projectId}/members")
    @Operation(summary = "Add an organization member to a project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddProjectMemberRequest request,
            @CurrentUser UserPrincipal principal) {
        ProjectMemberResponse response = projectService.addProjectMember(projectId, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Member added to project", response));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @Operation(summary = "Remove a member from a project")
    public ResponseEntity<ApiResponse<Void>> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal principal) {
        projectService.removeProjectMember(projectId, userId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Member removed from project", null));
    }
}
