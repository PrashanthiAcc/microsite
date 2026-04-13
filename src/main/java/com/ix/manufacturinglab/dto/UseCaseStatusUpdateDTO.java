package com.ix.manufacturinglab.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the status of a Use Case.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseCaseStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;
}
