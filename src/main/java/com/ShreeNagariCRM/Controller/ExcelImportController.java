package com.ShreeNagariCRM.Controller;

import com.ShreeNagariCRM.DTO.baseDto.ApiResponse;
import com.ShreeNagariCRM.Entity.DynamicData;
import com.ShreeNagariCRM.Entity.User;
import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import com.ShreeNagariCRM.Repository.DynamicDataRepository;
import com.ShreeNagariCRM.Repository.ExcelImportSessionRepository;
import com.ShreeNagariCRM.Service.ExcelImportService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<ApiResponse> preview(@RequestParam("file") MultipartFile file) throws Exception {
//        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded");
        ApiResponse result = excelImportService.previewImport(file);
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
    public ResponseEntity<?> confirm(
            @RequestParam("file")                                    MultipartFile file,
            @RequestParam("sessionId")                               String sessionId,
            @RequestParam(value = "nameColumn",      required = false) String nameColumn,
            @RequestParam(value = "phoneColumn",     required = false) String phoneColumn,
            @RequestParam(value = "emailColumn",     required = false) String emailColumn,
            @RequestParam(value = "assignToAgentId", required = false) Long agentId,
            @RequestParam(value = "defaultStatus",   required = false) String defaultStatus
    ) throws Exception {
        LeadStatus status = defaultStatus != null
                ? LeadStatus.valueOf(defaultStatus.toUpperCase())
                : LeadStatus.NEW;

        Map<String, Object> result = excelImportService.confirmImport(
                file, sessionId, nameColumn, phoneColumn, emailColumn, agentId, status);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all imported rows from one session.
     * GET /api/excel/sessions/{sessionId}/rows
     */
    @GetMapping("/sessions/{sessionId}/rows")
    public ResponseEntity<?> getRowsBySession(@PathVariable String sessionId) {
        return ResponseEntity.ok(dynamicDataRepository.findByUploadSessionId(sessionId));
    }

    /**
     * List all import sessions (history).
     * GET /api/excel/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions() {
        return ResponseEntity.ok(sessionRepository.findAllByOrderByUploadedAtDesc());
    }

    /**
     * Undo an import — deletes all rows from the session.
     * DELETE /api/excel/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
        excelImportService.deleteSession(sessionId);
        return ResponseEntity.ok(Map.of("deleted", true, "sessionId", sessionId));
    }

    /**
     * Get all dynamic data rows (pageable via query params).
     * GET /api/excel/rows?status=NEW&agentId=1&q=priya
     */
    @GetMapping("/rows")
    public ResponseEntity<?> getRows(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String q) {

        List<DynamicData> rows;
        if (q != null && !q.isBlank()) {
            rows = dynamicDataRepository.search(q);
        } else if (agentId != null) {
            rows = dynamicDataRepository.findByAssignedEmployee_Id(agentId);
        } else if (status != null) {
            rows = dynamicDataRepository.findByMappedLeadStatus(
                    LeadStatus.valueOf(status.toUpperCase()));
        } else {
            rows = dynamicDataRepository.findAll();
        }
        return ResponseEntity.ok(rows);
    }

    /**
     * Update status of a dynamic data row.
     * PATCH /api/excel/rows/{id}/status?status=CONTACTED
     */
    @PatchMapping("/rows/{id}/status")
    public ResponseEntity<?> updateRowStatus(@PathVariable Long id,
                                             @RequestParam String status) {
        DynamicData row = dynamicDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Row not found: " + id));
        row.setMappedLeadStatus(LeadStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(dynamicDataRepository.save(row));
    }

    /**
     * Assign a dynamic row to an agent.
     * PATCH /api/excel/rows/{id}/assign?agentId=2
     */
    @PatchMapping("/rows/{id}/assign")
    public ResponseEntity<?> assignRow(@PathVariable Long id,
                                       @RequestParam Long agentId) {
        DynamicData row = dynamicDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Row not found: " + id));
        User emp = new User(); emp.setId(agentId);
        row.setAssignedEmployee(emp);
        return ResponseEntity.ok(dynamicDataRepository.save(row));
    }
}
