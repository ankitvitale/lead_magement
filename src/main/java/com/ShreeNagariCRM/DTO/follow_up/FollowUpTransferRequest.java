package com.ShreeNagariCRM.DTO.follow_up;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowUpTransferRequest {

    @NotNull(message = "toEmployeeId is required")
    private Long toEmployeeId;

    @NotBlank(message = "reason is required")
    private String reason;
}
