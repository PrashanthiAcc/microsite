package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Industry API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryDTO {
    private Long industryId;
    private String industryName;
}
