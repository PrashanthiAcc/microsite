package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageResponseDTO {

    private String applicationName;
    private String title;
    private String subTitle;
    private String heroImageUrl;

    private String mesMomSolDelivered;
    private String prodSiteCriticalSupport;
    private String sapEwmPrgDelivered;

    private String keyCapabilityConfigurationImage;

    private Integer updatedById;

    private List<FeatureStoriesDTO> featuredStories;
    private List<IndustryThumbnailDTO> industryThumbnails;
}
