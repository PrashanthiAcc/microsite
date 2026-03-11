package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for returning Use Case details in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UseCaseResponseDTO {

    private Integer usecaseId;
    private Long industryId;
    private Long subIndustryId;
    private Integer valueChainId;
    private String title;
    private String thumbnailImageUrl;
    private String tag;
    private String description;
    private Integer duration;
    private Integer ownerId;
    private String primarySpeakerEid;
    private String secondarySpeakerEid;
    private String tertiarySpeakerEid;
    private String oneSliderUrl;
    private String multiSliderUrl;
    private String demoVideoUrl;
    private String clientTestimonialDemoUrl;
    private String narrationVideoUrl;
    private String businessProblem;
    private String solutions;
    private String valueDelivered;
    private String toolsAndTechnologies;
    private String keyResults;
    private String status;
    private Integer approverId;
    private LocalDateTime approvedDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Boolean isActive;
    private Integer creatorId;
}
