package com.ShreeNagariCRM.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "excel_import_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelImportSession {

    @Id
    @Column(length = 36)
    private String sessionId;           // UUID

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private Integer totalRows;

    @Column(nullable = false)
    private Integer importedRows;

    @Column(nullable = false)
    private Integer skippedRows;

    /**
     * JSON array of column headers found in this Excel file.
     * E.g.: ["Customer Name","Mobile","City","Budget","Property Type"]
     * Stored so the admin can later configure column mappings.
     */
    @Column(columnDefinition = "JSON")
    private String detectedHeaders;

    /**
     * JSON object: which header maps to name / email / phone.
     * E.g.: {"nameColumn":"Customer Name","phoneColumn":"Mobile","emailColumn":"Email ID"}
     * Null means auto-detected.
     */
    @Column(columnDefinition = "JSON")
    private String columnMapping;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ImportStatus status = ImportStatus.COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_agent_id")
    private User uploadedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    public enum ImportStatus {
        PROCESSING, COMPLETED, FAILED, PARTIALLY_IMPORTED
    }
}
