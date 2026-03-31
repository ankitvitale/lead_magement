package com.ShreeNagariCRM.DTO.follow_up;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowUpTransferLogResponse {

    private Long id;
    private Long followUpId;
    private String leadName;

    // From
    private Long fromEmployeeId;
    private String fromEmployeeName;

    // To
    private Long toEmployeeId;
    private String toEmployeeName;

    // Who transferred
    private Long transferredById;
    private String transferredByName;

    private String reason;
    private LocalDateTime transferredAt;
}
