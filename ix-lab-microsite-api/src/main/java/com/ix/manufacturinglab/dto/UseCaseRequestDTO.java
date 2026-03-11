package com.ix.manufacturinglab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new Use Case with all related data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseCaseRequestDTO {

    @NotNull(message = "Industry ID is required")
    private Long industryId;

    @NotNull(message = "Sub-Industry ID is required")
    private Long subIndustryId;

    @NotNull(message = "Value Chain ID is required")
    private Integer valueChainId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 500, message = "Thumbnail image URL must not exceed 500 characters")
    private String thumbnailImageUrl;

    @Size(max = 100, message = "Tag must not exceed 100 characters")
    private String tag;

    private String description;

    private Integer duration;

    @NotNull(message = "Owner ID is required")
    private Integer ownerId;

    @Size(max = 100, message = "Primary speaker EID must not exceed 100 characters")
    private String primarySpeakerEid;

    @Size(max = 100, message = "Secondary speaker EID must not exceed 100 characters")
    private String secondarySpeakerEid;

    @Size(max = 100, message = "Tertiary speaker EID must not exceed 100 characters")
    private String tertiarySpeakerEid;

    @Size(max = 500, message = "One slider URL must not exceed 500 characters")
    private String oneSliderUrl;

    @Size(max = 500, message = "Multi slider URL must not exceed 500 characters")
    private String multiSliderUrl;

    @Size(max = 500, message = "Demo video URL must not exceed 500 characters")
    private String demoVideoUrl;

    @Size(max = 500, message = "Client testimonial URL must not exceed 500 characters")
    private String clientTestimonialDemoUrl;

    @Size(max = 500, message = "Narration video URL must not exceed 500 characters")
    private String narrationVideoUrl;

    private String businessProblem;

    private String solutions;

    private String valueDelivered;

    private String toolsAndTechnologies;

    private String keyResults;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Integer approverId;

    private Boolean isActive;
    private Integer creatorId;
}
