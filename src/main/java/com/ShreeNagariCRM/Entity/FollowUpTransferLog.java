package com.ShreeNagariCRM.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "follow_up_transfer_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowUpTransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Which follow-up was transferred ───────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_up_id", nullable = false)
    private FollowUp followUp;

    // ── From which employee ───────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_employee_id")
    private User fromEmployee;

    private String fromEmployeeName;   // store name even if employee deleted

    // ── To which employee ─────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_employee_id")
    private User toEmployee;

    private String toEmployeeName;     // store name even if employee deleted

    // ── Who did the transfer (always admin) ───────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_by_id")
    private User transferredBy;

    private String transferredByName;

    // ── Why transferred ───────────────────────────────────────────────────────
    @Column(nullable = false)
    private String reason;

    // ── When ─────────────────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime transferredAt;
}
