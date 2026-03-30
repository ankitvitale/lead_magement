package com.ShreeNagariCRM.Controller;


import com.ShreeNagariCRM.Entity.FollowUp;
import com.ShreeNagariCRM.Entity.enums.FollowUpType;
import com.ShreeNagariCRM.Entity.enums.Priority;
import com.ShreeNagariCRM.Service.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FollowUpController {

    private final FollowUpService followUpService;

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {

        Long leadId = getLong(body, "leadId");
        Long dynamicDataId = getLong(body, "dynamicDataId");
        Long agentId = getLong(body, "agentId");

        LocalDate scheduledDate = LocalDate.parse((String) body.get("scheduledDate"));
        LocalTime scheduledTime = LocalTime.parse((String) body.get("scheduledTime"));

        FollowUpType type = FollowUpType.valueOf((String) body.get("type"));
        Priority priority = body.containsKey("priority")
                ? Priority.valueOf((String) body.get("priority"))
                : Priority.MEDIUM;

        String notes = (String) body.getOrDefault("notes", null);

        FollowUp saved = followUpService.create(
                leadId, dynamicDataId, agentId,
                scheduledDate, scheduledTime, type, priority, notes);

        return ResponseEntity.ok(toResponse(saved));
    }

    // ── READ ALL (with optional filters) ─────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) Long dynamicDataId) {

        List<FollowUp> list;

        if (agentId != null) {
            list = followUpService.getByAgent(agentId);
        } else if (leadId != null) {
            list = followUpService.getByStandardLead(leadId);
        } else if (dynamicDataId != null) {
            list = followUpService.getByDynamicData(dynamicDataId);
        } else if ("overdue".equalsIgnoreCase(filter)) {
            list = followUpService.getOverdue();
        } else if ("today".equalsIgnoreCase(filter)) {
            list = followUpService.getToday();
        } else if ("upcoming".equalsIgnoreCase(filter)) {
            list = followUpService.getUpcoming();
        } else if ("pending".equalsIgnoreCase(filter)) {
            list = followUpService.getPending();
        } else {
            list = followUpService.getAll();
        }

        return ResponseEntity.ok(list.stream().map(this::toResponse).toList());
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(followUpService.getById(id)));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {

        LocalDate date = body.containsKey("scheduledDate")
                ? LocalDate.parse((String) body.get("scheduledDate")) : null;
        LocalTime time = body.containsKey("scheduledTime")
                ? LocalTime.parse((String) body.get("scheduledTime")) : null;
        FollowUpType type = body.containsKey("type")
                ? FollowUpType.valueOf((String) body.get("type")) : null;
        Priority priority = body.containsKey("priority")
                ? Priority.valueOf((String) body.get("priority")) : null;
        String notes = (String) body.getOrDefault("notes", null);

        return ResponseEntity.ok(toResponse(
                followUpService.update(id, date, time, type, priority, notes)));
    }

    // ── MARK DONE ─────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/done")
    public ResponseEntity<?> markDone(@PathVariable Long id,
                                      @RequestBody(required = false) Map<String, String> body) {
        String outcome = body != null ? body.get("outcome") : null;
        return ResponseEntity.ok(toResponse(followUpService.markDone(id, outcome)));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        followUpService.delete(id);
        return ResponseEntity.ok(Map.of("deleted", true, "id", id));
    }

    // ── SHORTCUTS ─────────────────────────────────────────────────────────────
    @GetMapping("/overdue")
    public ResponseEntity<?> overdue() {
        return ResponseEntity.ok(followUpService.getOverdue().stream().map(this::toResponse).toList());
    }

    @GetMapping("/today")
    public ResponseEntity<?> today() {
        return ResponseEntity.ok(followUpService.getToday().stream().map(this::toResponse).toList());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> upcoming() {
        return ResponseEntity.ok(followUpService.getUpcoming().stream().map(this::toResponse).toList());
    }

    @GetMapping("/by-lead/{leadId}")
    public ResponseEntity<?> byLead(@PathVariable Long leadId) {
        return ResponseEntity.ok(followUpService.getByStandardLead(leadId)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/by-dynamic/{dynamicDataId}")
    public ResponseEntity<?> byDynamic(@PathVariable Long dynamicDataId) {
        return ResponseEntity.ok(followUpService.getByDynamicData(dynamicDataId)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/by-agent/{agentId}")
    public ResponseEntity<?> byAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(followUpService.getByAgent(agentId)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(followUpService.getDashboardStats());
    }

    // ── Response Mapper ───────────────────────────────────────────────────────
    private Map<String, Object> toResponse(FollowUp f) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("followUpId", f.getFollowUpId());
        map.put("leadDisplayName", f.getLeadDisplayName());
        map.put("leadPhone", f.getLeadPhone());
        map.put("leadEmail", f.getLeadEmail());
        map.put("leadId", f.getLead() != null ? f.getLead().getId() : null);
        map.put("dynamicDataId", f.getDynamicData() != null ? f.getDynamicData().getId() : null);
        map.put("isDynamicLead", f.isForDynamicLead());
        map.put("agentId", f.getAssignedTo() != null ? f.getAssignedTo().getId() : null);
        map.put("agentName", f.getAssignedTo() != null ? f.getAssignedTo().getName() : null);
        map.put("scheduledDate", f.getScheduledDate());
        map.put("scheduledTime", f.getScheduledTime());
        map.put("type", f.getType());
        map.put("priority", f.getPriority());
        map.put("notes", f.getNotes());
        map.put("outcome", f.getOutcome());
        map.put("done", f.getDone());
        map.put("doneAt", f.getDoneAt());
        map.put("createdAt", f.getCreatedAt());
        return map;
    }

    private Long getLong(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        if (val instanceof Integer i) return i.longValue();
        if (val instanceof Long l) return l;
        return Long.parseLong(val.toString());
    }

}