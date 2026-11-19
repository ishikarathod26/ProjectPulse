package io.projectpulse.backend.activity;

import io.projectpulse.backend.activity.dto.ActivityBatchRequest;
import io.projectpulse.backend.activity.dto.ActivityBatchResponse;
import io.projectpulse.backend.activity.dto.ActivityResponse;
import io.projectpulse.backend.auth.CurrentUser;
import io.projectpulse.backend.auth.UserPrincipal;
import io.projectpulse.backend.common.ApiResponse;
import io.projectpulse.backend.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@Tag(name = "Activities", description = "Endpoints for activity event ingestion, timeline feeds, filtering, and stats")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/batch")
    @Operation(summary = "Batch upload file activity events from desktop tracking agent")
    public ResponseEntity<ApiResponse<ActivityBatchResponse>> ingestBatch(
            @Valid @RequestBody ActivityBatchRequest request,
            @CurrentUser UserPrincipal principal) {
        ActivityBatchResponse response = activityService.ingestBatch(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Batch processed", response));
    }

    @GetMapping
    @Operation(summary = "Retrieve paginated project activity history with filtering")
    public ResponseEntity<ApiResponse<PagedResponse<ActivityResponse>>> getProjectActivities(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser UserPrincipal principal) {
        PagedResponse<ActivityResponse> activities = activityService.getProjectActivities(
                projectId, memberId, eventType, startDate, endDate, page, size, principal.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @GetMapping("/stats")
    @Operation(summary = "Retrieve aggregate activity counts and metrics for project dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectActivityStats(
            @RequestParam Long projectId,
            @CurrentUser UserPrincipal principal) {
        Map<String, Object> stats = activityService.getProjectActivityStats(projectId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
