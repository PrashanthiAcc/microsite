package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Integer updatedById;
}
