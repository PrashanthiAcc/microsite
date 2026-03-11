package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Use Case search/filter criteria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseCaseFilterDTO {

    private String keyword;
    private String category;
    private String status;
    private String priority;
    private String complexity;
    private String industrySegment;
    private String owner;
    private Boolean isActive;
}
