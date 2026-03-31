package com.ShreeNagariCRM.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Which employee did the action ─────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    // ── What action was performed ─────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityAction action;

    // ── On which module (FollowUp, Lead, DynamicData etc.) ────────────────────
    @Column(nullable = false)
    private ActivityModule module;          // "FOLLOW_UP" / "LEAD" / "DYNAMIC_DATA"

    // ── On which record ───────────────────────────────────────────────────────
    private Long recordId;          // id of the affected record

    // ── Description of what changed ───────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String description;     // e.g. "Updated status to CONTACTED"

    // ── Extra details (JSON) ──────────────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String metadata;        // optional JSON for extra info

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime performedAt;

    public enum ActivityAction {
        CREATED,
        UPDATED,
        DELETED,
        STATUS_CHANGED,
        FOLLOW_UP_CREATED,
        FOLLOW_UP_UPDATED,
        FOLLOW_UP_DONE,
        FOLLOW_UP_TRANSFERRED,
        LEAD_ASSIGNED,
        EXCEL_IMPORTED
    }

    public enum ActivityModule{
        FOLLOW_UP,LEAD,DYNAMIC_DATA
    }
}