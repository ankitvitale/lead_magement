package com.ShreeNagariCRM.Controller;


import com.ShreeNagariCRM.DTO.activityDto.ActivityLogResponse;
import com.ShreeNagariCRM.DTO.baseDto.ApiResponse;
import com.ShreeNagariCRM.DTO.follow_up.*;
import com.ShreeNagariCRM.Entity.EmployeeActivityLog;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Security.CustomUserDetails;
import com.ShreeNagariCRM.Service.ActivityLogService;
import com.ShreeNagariCRM.Service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FollowUpController {

    private final FollowUpService followUpService;
    private final ActivityLogService activityLogService;

    // ── Get current user helper ───────────────────────────────────────────────
    private User getCurrentUser(Authentication auth) {
        return ((CustomUserDetails) auth.getPrincipal()).getUser();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // Admin  → can assign to any employee via agentId
    // User   → auto-assigned to themselves
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> create(
            @Valid @RequestBody FollowUpRequest request,
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.create(request, getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL
    // Admin → all | User → own only
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getAll(
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.getAll(getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY ID
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> getById(
            @PathVariable Long id,
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.getById(id, getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET TODAY
    // Admin → all today | User → own today
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getToday(
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.getToday(getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET OVERDUE
    // Admin → all overdue | User → own overdue
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getOverdue(
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.getOverdue(getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET UPCOMING
    // Admin → all upcoming | User → own upcoming
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getUpcoming(
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.getUpcoming(getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY STANDARD LEAD
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/by-lead/{leadId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getByLead(
            @PathVariable Long leadId,
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.getByLeadId(leadId, getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET BY DYNAMIC LEAD
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/by-dynamic/{dynamicDataId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getByDynamic(
            @PathVariable Long dynamicDataId,
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.getByDynamicDataId(dynamicDataId,
                        getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> update(
            @PathVariable Long id,
            @RequestBody FollowUpRequest request,
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.update(id, request, getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MARK DONE
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/done")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> markDone(
            @PathVariable Long id,
            @RequestParam(required = false) String outcome,
            Authentication auth) {

       // String outcome = (body != null) ? body.get("outcome") : null;
        return ResponseEntity.ok(
                followUpService.markDone(id, outcome, getCurrentUser(auth)));
    }

//    // ─────────────────────────────────────────────────────────────────────────
//    // TRANSFER — ADMIN ONLY
//    // ─────────────────────────────────────────────────────────────────────────
//    @PostMapping("/{id}/transfer")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ApiResponse<FollowUpResponse>> transfer(
//            @PathVariable Long id,
//            @Valid @RequestBody FollowUpTransferRequest request,
//            Authentication auth) {
//
//        return ResponseEntity.ok(
//                followUpService.transfer(id, request, getCurrentUser(auth)));
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // GET TRANSFER HISTORY — ADMIN ONLY
//    // ─────────────────────────────────────────────────────────────────────────
//    @GetMapping("/{id}/transfer-history")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ApiResponse<List<FollowUpTransferLogResponse>>>
//    getTransferHistory(
//            @PathVariable Long id,
//            Authentication auth) {
//
//        return ResponseEntity.ok(
//                followUpService.getTransferHistory(id, getCurrentUser(auth)));
//    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE — ADMIN ONLY
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Authentication auth) {

        return ResponseEntity.ok(
                followUpService.delete(id, getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MY ACTIVITY LOG — EMPLOYEE ONLY (sees only their own)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/my-activity")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getMyActivity(
            Authentication auth) {

        return ResponseEntity.ok(
                activityLogService.getMyLogs(getCurrentUser(auth)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MY ACTIVITY LOG BY MODULE
//    // ─────────────────────────────────────────────────────────────────────────
//    @GetMapping("/my-activity/{module}")
//    @PreAuthorize("hasAnyRole('ADMIN','USER')")
//    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>>
//    getMyActivityByModule(
//            @PathVariable EmployeeActivityLog.ActivityModule module,
//            Authentication auth) {
//
//        return ResponseEntity.ok(
//                activityLogService.getMyLogsByModule(
//                        getCurrentUser(auth), module));
//    }
}