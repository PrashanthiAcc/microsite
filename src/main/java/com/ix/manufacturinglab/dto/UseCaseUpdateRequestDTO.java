package com.ix.manufacturinglab.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing Use Case.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseCaseUpdateRequestDTO {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    @Size(max = 20, message = "Priority must not exceed 20 characters")
    private String priority;

    @Size(max = 100, message = "Industry segment must not exceed 100 characters")
    private String industrySegment;

    @Size(max = 500, message = "Technology stack must not exceed 500 characters")
    private String technologyStack;

    private String businessValue;

    @Size(max = 50, message = "Estimated effort must not exceed 50 characters")
    private String estimatedEffort;

    @Size(max = 20, message = "Complexity must not exceed 20 characters")
    private String complexity;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @Size(max = 100, message = "Owner must not exceed 100 characters")
    private String owner;
}
