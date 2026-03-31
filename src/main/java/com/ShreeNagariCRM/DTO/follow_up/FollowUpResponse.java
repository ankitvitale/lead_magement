package com.ShreeNagariCRM.DTO.follow_up;


import com.ShreeNagariCRM.Entity.enums.FollowUpType;
import com.ShreeNagariCRM.Entity.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowUpResponse {

    private Long followUpId;

    // ── Lead Info ─────────────────────────────────────────────────────────────
    private Long leadId;              // null if dynamic lead
    private Long dynamicDataId;       // null if standard lead
    private Boolean isDynamicLead;    // true = excel lead, false = standard lead
    private String leadDisplayName;   // name of the lead
    private String leadPhone;
    private String leadEmail;

    // ── Agent Info ────────────────────────────────────────────────────────────
    private Long agentId;
    private String agentName;

    // ── Schedule ──────────────────────────────────────────────────────────────
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;

    // ── Type & Priority ───────────────────────────────────────────────────────
    private FollowUpType type;
    private Priority priority;

    // ── Content ───────────────────────────────────────────────────────────────
    private String notes;
    private String outcome;

    // ── Status ────────────────────────────────────────────────────────────────
    private Boolean done;
    private LocalDateTime doneAt;

    // ── Timestamps ────────────────────────────────────────────────────────────
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
