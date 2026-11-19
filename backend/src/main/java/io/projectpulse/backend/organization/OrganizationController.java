package io.projectpulse.backend.organization;

import io.projectpulse.backend.auth.CurrentUser;
import io.projectpulse.backend.auth.UserPrincipal;
import io.projectpulse.backend.common.ApiResponse;
import io.projectpulse.backend.organization.dto.CreateOrgRequest;
import io.projectpulse.backend.organization.dto.InviteMemberRequest;
import io.projectpulse.backend.organization.dto.OrgMemberResponse;
import io.projectpulse.backend.organization.dto.OrgResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organizations", description = "Multi-tenant organization workspaces and member management")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Create a new organization workspace")
    public ResponseEntity<ApiResponse<OrgResponse>> createOrganization(
            @Valid @RequestBody CreateOrgRequest request,
            @CurrentUser UserPrincipal principal) {
        OrgResponse response = organizationService.createOrganization(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Organization created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all organizations the authenticated user belongs to")
    public ResponseEntity<ApiResponse<List<OrgResponse>>> getUserOrganizations(@CurrentUser UserPrincipal principal) {
        List<OrgResponse> responses = organizationService.getUserOrganizations(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{orgId}")
    @Operation(summary = "Get organization details by ID")
    public ResponseEntity<ApiResponse<OrgResponse>> getOrganizationById(
            @PathVariable Long orgId,
            @CurrentUser UserPrincipal principal) {
        OrgResponse response = organizationService.getOrganizationById(orgId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{orgId}/members")
    @Operation(summary = "List all members of an organization")
    public ResponseEntity<ApiResponse<List<OrgMemberResponse>>> getOrganizationMembers(
            @PathVariable Long orgId,
            @CurrentUser UserPrincipal principal) {
        List<OrgMemberResponse> members = organizationService.getOrganizationMembers(orgId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping("/{orgId}/members")
    @Operation(summary = "Invite or add a user to the organization")
    public ResponseEntity<ApiResponse<OrgMemberResponse>> inviteMember(
            @PathVariable Long orgId,
            @Valid @RequestBody InviteMemberRequest request,
            @CurrentUser UserPrincipal principal) {
        OrgMemberResponse response = organizationService.inviteMember(orgId, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Member added to organization", response));
    }

    @DeleteMapping("/{orgId}/members/{userId}")
    @Operation(summary = "Remove a user from an organization")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long orgId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal principal) {
        organizationService.removeMember(orgId, userId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
