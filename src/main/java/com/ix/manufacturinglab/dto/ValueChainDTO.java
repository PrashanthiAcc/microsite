package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Value Chain API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueChainDTO {

    private Long valueChainId;
    private String valueChainName;
    private Long industryId;
    private Long subIndustryId;
    private Integer updatedById;
    private LocalDateTime lastUpdated;
}
