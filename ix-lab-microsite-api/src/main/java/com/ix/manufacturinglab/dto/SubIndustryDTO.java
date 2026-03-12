package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Sub-Industry API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubIndustryDTO {

    private Long subIndustryId;
    private String subIndustryName;
    private Long industryId;
}
