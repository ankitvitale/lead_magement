package com.ShreeNagariCRM.Controller;

import com.ShreeNagariCRM.DTO.baseDto.ApiResponse;
import com.ShreeNagariCRM.DTO.excelDto.BulkAssignRequest;
import com.ShreeNagariCRM.DTO.excelDto.DynamicDataResponseDto;
import com.ShreeNagariCRM.DTO.excelDto.ExcelImportResultDto;
import com.ShreeNagariCRM.DTO.excelDto.ExcelSessionResponseDto;
import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Repository.DynamicDataRepository;
import com.ShreeNagariCRM.Repository.ExcelImportSessionRepository;
import com.ShreeNagariCRM.Security.CustomUserDetails;
import com.ShreeNagariCRM.Service.ExcelImportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ShreeNagariCRM.DTO.baseDto.ApiResponse;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExcelImportController {

    private final ExcelImportService excelImportService;
    private final DynamicDataRepository dynamicDataRepository;
    private final ExcelImportSessionRepository sessionRepository;

    /**
     * STEP 1 — Preview the uploaded file.
     * Returns headers + first 5 rows + auto-detected column mapping.
     * Does NOT persist rows yet.
     *
     * POST /api/excel/preview
     * Content-Type: multipart/form-data
     * Body: file = <xlsx file>
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse> preview(@RequestParam("file") MultipartFile file, Authentication authentication) throws Exception {
//        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded");

        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        ApiResponse result = excelImportService.previewImport(file,user);
        return ResponseEntity.ok(result);
    }

    /**
     * STEP 2 — Confirm import with column mapping.
     * Persists all rows as DynamicData.
     *
     * POST /api/excel/confirm
     * Content-Type: multipart/form-data
     * Body:
     *   file            = <same xlsx file>
     *   sessionId       = UUID from preview response
     *   nameColumn      = "Customer Name"   (exact header from your Excel)
     *   phoneColumn     = "Mobile"
     *   emailColumn     = "Email ID"        (optional)
     *   assignToAgentId = 1                 (optional)
     *   defaultStatus   = "NEW"             (optional, default: NEW)
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<ExcelImportResultDto>> confirm(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "nameColumn", required = false) String nameColumn,
            @RequestParam(value = "phoneColumn", required = false) String phoneColumn,
            @RequestParam(value = "emailColumn", required = false) String emailColumn,
            @RequestParam(value = "defaultStatus", required = false) String defaultStatus,
            Authentication authentication
    ) throws Exception {
        User currentUser = getCurrentUser(authentication);

        LeadStatus status = defaultStatus != null
                ? LeadStatus.valueOf(defaultStatus.toUpperCase())
                : LeadStatus.NEW;

        ApiResponse<ExcelImportResultDto> result = excelImportService.confirmImport(
                file, sessionId, nameColumn, phoneColumn, emailColumn, status,currentUser);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all imported rows from one session.
     * GET /api/excel/sessions/{sessionId}/rows
     */
    @GetMapping("/sessions/{sessionId}/rows")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<DynamicDataResponseDto>>> getRowsBySession(@PathVariable String sessionId,Authentication authentication) {

        return ResponseEntity.status(HttpStatus.OK).body(excelImportService.getRowsBySession(sessionId,getCurrentUser(authentication)));
    }

    /**
     * List all import sessions (history).
     * GET /api/excel/sessions
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<ExcelSessionResponseDto>>> getSessions(Authentication authentication) {
        return ResponseEntity.ok(excelImportService.getSessions(getCurrentUser(authentication)));
    }

    // GET /api/excel/sessions/{sessionId}/unassigned
    @GetMapping("/sessions/{sessionId}/unassigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DynamicDataResponseDto>>> getUnassignedRows(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(excelImportService.getUnassignedRows(sessionId));
    }

//    /**
//     * Undo an import — deletes all rows from the session.
//     * DELETE /api/excel/sessions/{sessionId}
//     */
//    @DeleteMapping("/sessions/{sessionId}")
//    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
//        excelImportService.deleteSession(sessionId);
//        return ResponseEntity.ok(Map.of("deleted", true, "sessionId", sessionId));
//    }

    /**
     * Get all dynamic data rows (pageable via query params).
     * GET /api/excel/rows?status=NEW&agentId=1&q=priya
     */
//    @GetMapping("/rows")
//    @PreAuthorize("hasAnyRole('ADMIN','USER')")
//    public ResponseEntity<?> getRows(
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) Long agentId,
//            @RequestParam(required = false) String q) {
//
//        List<DynamicData> rows;
//        if (q != null && !q.isBlank()) {
//            rows = dynamicDataRepository.search(q);
//        } else if (agentId != null) {
//            rows = dynamicDataRepository.findByAssignedEmployee_Id(agentId);
//        } else if (status != null) {
//            rows = dynamicDataRepository.findByMappedLeadStatus(
//                    LeadStatus.valueOf(status.toUpperCase()));
//        } else {
//            rows = dynamicDataRepository.findAll();
//        }
//        return ResponseEntity.ok(rows);
//    }

    /**
     * Update status of a dynamic data row.
     * PATCH /api/excel/rows/{id}/status?status=CONTACTED
     */
    @PatchMapping("/rows/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<DynamicDataResponseDto>> updateRowStatus(@PathVariable Long id,
                                             @RequestParam LeadStatus status,Authentication authentication) {
      return ResponseEntity.ok(excelImportService.updateRowStatus(id,status,getCurrentUser(authentication)));
    }

    /**
     * Assign a dynamic row to an agent.
     * PATCH /api/excel/rows/{id}/assign?agentId=2
     */
    @PatchMapping("/rows/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DynamicDataResponseDto>>> assignMultipleRows(@RequestBody BulkAssignRequest bulkAssignRequest) {
       return ResponseEntity.ok(excelImportService.assignMultipleRows(bulkAssignRequest));
    }

    // ── Helper to get current logged-in user ──────────────────────────────────
    private User getCurrentUser(Authentication authentication) {
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }
}
