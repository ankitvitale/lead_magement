package com.ShreeNagariCRM.DTO.activityDto;


import com.ShreeNagariCRM.Entity.EmployeeActivityLog;
import com.ShreeNagariCRM.Entity.EmployeeActivityLog.ActivityAction;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLogResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private ActivityAction action;
    private EmployeeActivityLog.ActivityModule module;
    private Long recordId;
    private String description;
    private LocalDateTime performedAt;
}