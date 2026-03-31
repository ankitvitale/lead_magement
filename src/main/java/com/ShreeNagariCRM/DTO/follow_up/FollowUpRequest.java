package com.ShreeNagariCRM.DTO.follow_up;


import com.ShreeNagariCRM.Entity.enums.FollowUpType;
import com.ShreeNagariCRM.Entity.enums.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowUpRequest {

    // ── Lead Source (exactly ONE must be provided) ────────────────────────────
    private Long leadId;          // standard lead
    private Long dynamicDataId;   // excel imported lead

    // ── Agent ─────────────────────────────────────────────────────────────────
    private Long agentId;         // optional — defaults to lead's assigned agent

    // ── Schedule ──────────────────────────────────────────────────────────────
    @NotNull(message = "scheduledDate is required")
    private LocalDate scheduledDate;

    @NotNull(message = "scheduledTime is required")
    private LocalTime scheduledTime;

    // ── Type & Priority ───────────────────────────────────────────────────────
    @NotNull(message = "type is required")
    private FollowUpType type;

    private Priority priority = Priority.MEDIUM;  // default MEDIUM

    // ── Notes ─────────────────────────────────────────────────────────────────
    private String notes;
}