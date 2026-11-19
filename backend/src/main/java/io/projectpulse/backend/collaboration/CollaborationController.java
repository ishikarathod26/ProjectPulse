package io.projectpulse.backend.collaboration;

import io.projectpulse.backend.auth.CurrentUser;
import io.projectpulse.backend.auth.UserPrincipal;
import io.projectpulse.backend.collaboration.dto.*;
import io.projectpulse.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
@Tag(name = "Team Collaboration", description = "Endpoints for project chat, activity-linked notes, and status updates")
public class CollaborationController {

    private final CollaborationService collaborationService;

    public CollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @PostMapping("/messages")
    @Operation(summary = "Post a message to project team chat")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long projectId,
            @Valid @RequestBody SendMessageRequest request,
            @CurrentUser UserPrincipal principal) {
        MessageResponse response = collaborationService.sendMessage(projectId, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/messages")
    @Operation(summary = "Get recent messages from project team chat")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getRecentMessages(
            @PathVariable Long projectId,
            @CurrentUser UserPrincipal principal) {
        List<MessageResponse> responses = collaborationService.getRecentMessages(projectId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/notes")
    @Operation(summary = "Create a project note (optionally linked to a specific activity)")
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateNoteRequest request,
            @CurrentUser UserPrincipal principal) {
        NoteResponse response = collaborationService.createNote(projectId, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Note created", response));
    }

    @GetMapping("/notes")
    @Operation(summary = "List all notes created for the project")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getProjectNotes(
            @PathVariable Long projectId,
            @CurrentUser UserPrincipal principal) {
        List<NoteResponse> responses = collaborationService.getProjectNotes(projectId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/status-updates")
    @Operation(summary = "Post a project status update")
    public ResponseEntity<ApiResponse<StatusUpdateResponse>> createStatusUpdate(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateStatusUpdateRequest request,
            @CurrentUser UserPrincipal principal) {
        StatusUpdateResponse response = collaborationService.createStatusUpdate(projectId, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Status update posted", response));
    }

    @GetMapping("/status-updates")
    @Operation(summary = "List recent project status updates")
    public ResponseEntity<ApiResponse<List<StatusUpdateResponse>>> getStatusUpdates(
            @PathVariable Long projectId,
            @CurrentUser UserPrincipal principal) {
        List<StatusUpdateResponse> responses = collaborationService.getStatusUpdates(projectId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
