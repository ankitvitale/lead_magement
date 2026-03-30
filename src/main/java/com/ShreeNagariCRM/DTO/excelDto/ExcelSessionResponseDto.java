package com.ShreeNagariCRM.DTO.excelDto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelSessionResponseDto {

    private String sessionId;
    private String originalFileName;
    private Integer totalRows;
    private Integer importedRows;
    private Integer skippedRows;
    private String importStatus;        // COMPLETED / PARTIALLY_IMPORTED
    private LocalDateTime uploadedAt;

    // Agent who uploaded
    private Long uploadedById;
    private String uploadedByName;

    // ✅ Count of each LeadStatus inside this session
    // e.g. { "NEW": 10, "CONTACTED": 3, "QUALIFIED": 2 }
    private Map<String, Long> leadStatusCount;

    // ✅ Total rows in this session (double check)
    private Long totalDynamicRows;

    private long assignedCount;      // ✅ rows with assignedEmployee != null
    private long unassignedCount;    // ✅ rows with unassignedEmployee == null
}