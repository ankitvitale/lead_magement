package com.ShreeNagariCRM.DTO.excelDto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelImportResultDto {

    private String sessionId;
    private String fileName;
    private int importedRows;
    private int skippedRows;
    private List<String> skippedReasons;
    private String status;
}