package com.ix.manufacturinglab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private Long subIndustryId;

    private Integer valueChainId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 500, message = "Thumbnail image URL must not exceed 500 characters")
    private String thumbnailImageUrl;

    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    private String bannerUrl;

    @Size(max = 100, message = "Tag must not exceed 100 characters")

    private List<String> tags;

    private String description;

    private Integer duration;

    @NotNull(message = "Owner ID is required")
    private String ownerEId;

    private List<SpeakerDTO> speakers;

    private List<ArtifactDTO> artifacts;

    private List<FaqDTO> faq;

    private String businessProblem;

    private String narrationGuide;

    private String solutions;

    private String valueDelivered;

    private String toolsAndTechnologies;

    private String keyResults;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    private Integer approverId;

    private Boolean isActive;

    private Integer creatorId;

    private Integer updatedById;

}
