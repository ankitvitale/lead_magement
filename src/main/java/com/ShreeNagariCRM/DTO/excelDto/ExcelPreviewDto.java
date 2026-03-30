package com.ShreeNagariCRM.DTO.excelDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelPreviewDto {

    private String sessionId;
    private String fileName;
    private String sheetName;

    private List<String> headers;

    private int totalDataRows;

    // Each row = columnName → value
    private List<Map<String, String>> previewRows;

    // Suggested mapping for frontend dropdown
    private SuggestedMapping suggestedMapping;
}

