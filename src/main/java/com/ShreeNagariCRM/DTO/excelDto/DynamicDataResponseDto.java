package com.ShreeNagariCRM.DTO.excelDto;


import com.ShreeNagariCRM.Entity.enums.LeadStatus;
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
public class DynamicDataResponseDto {

    private Long id;

    // ✅ This is Map not String — so no backslashes in response
    private Map<String, Object> data;

   // private String fileName;
    private Integer rowNumber;
   // private String sheetName;
   // private String resolvedName;
   // private String resolvedEmail;
   // private String resolvedPhone;
    private LeadStatus status;
  //  private String uploadSessionId;
    private LocalDateTime uploadedAt;

    // Agent info flat (avoid lazy loading issues)
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
}