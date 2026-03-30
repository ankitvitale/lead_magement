package com.ShreeNagariCRM.DTO.excelDto;

import lombok.Data;

import java.util.List;

@Data
public class BulkAssignRequest {
    private List<Long> rowIds;
    private Long agentId;
}