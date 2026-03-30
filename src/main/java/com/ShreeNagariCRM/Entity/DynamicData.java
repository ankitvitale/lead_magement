package com.ShreeNagariCRM.Entity;

import com.ShreeNagariCRM.Entity.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dynamic_data")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DynamicData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "json",nullable = false)
    private String data;

    @Column(nullable = false)
    private String fileName;   // NEW FIELD

    @Column(name = "excel_row_number")
    private Integer rowNumber; // row index in the Excel sheet (1-based)

    @Column
    private String sheetName;         // Excel sheet/tab name

    // ── Resolved Contact Fields ───────────────────────────────────────────────
    // These are extracted from `data` at import time using column hints.
    // Stored separately so queries/follow-ups don't need to parse JSON.
    @Column
    private String resolvedName;      // extracted from "Name"/"Customer Name"/"Client" etc.

    @Column
    private String resolvedEmail;     // extracted from "Email"/"Mail" etc.

    @Column
    private String resolvedPhone;     // extracted from "Phone"/"Mobile"/"Contact" etc.

    @Enumerated(EnumType.STRING)
    @Column
    @Builder.Default
    private LeadStatus mappedLeadStatus = LeadStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_emp_id")
    private User assignedEmployee;

    // ── Upload Session ────────────────────────────────────────────────────────
    /**
     * Groups all rows from the same upload together.
     * Useful for "undo this import" or showing all rows from one file.
     */
    @Column(nullable = false)
    private String uploadSessionId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

}
