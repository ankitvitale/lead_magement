package com.ShreeNagariCRM.DTO.excelDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedMapping {

    private String nameColumn;
    private String phoneColumn;
    private String emailColumn;

    private String confidence; // HIGH / MEDIUM / LOW
}
