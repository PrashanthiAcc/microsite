package com.ix.manufacturinglab.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class HomePageConfigurationDTO {
    private Long id;

    private String applicationName;
    private String title;
    private String subtitle;
    private String heroImageUrl;

    private String mesMomSolDelivered;
    private String prodSiteCriticalSupport;
    private String sapEwmPrgDelivered;

    private String keyCapConfUrl;

    private LocalDateTime lastUpdated;
    private Integer updatedById;
}
