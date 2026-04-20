package com.ix.manufacturinglab.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePageRequestDTO {

    @NotNull(message = "Application Name is Required")
    @Size(max = 255, message = "Application Name not exceed 255 characters")
    private String applicationName;

    @NotNull(message = "Title is Required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Sub Title is Required")
    @Size(max = 255, message = "Sub Title must not exceed 255 characters")
    private String subTitle;

    @NotNull(message = "Hero Image is Required")
    @Size(max = 500, message = "Hero image URL must not exceed 500 characters")
    private String heroImageUrl;



    @NotNull(message = "MES/MOM solutions Delivered is Required")
    @Size(max = 50, message = "MES/MOM solutions Delivered must not exceed 500 characters")
    private String mesMomSolDelivered;

    @NotNull(message = "Production Site Support for Critical apps is Required")
    @Size(max = 50, message = "Production Site Support for Critical apps must not exceed 500 characters")
    private String prodSiteCriticalSupport;

    @NotNull(message = "SAP EWM Programs Delivered is Required")
    @Size(max = 50, message = "MES/MOM solutions Delivered must not exceed 500 characters")
    private String sapEwmPrgDelivered;

    private List<IndustryThumbnailDTO> industryThumbnails;

    private List<FeatureStoriesDTO> featuredStories;

    @NotNull(message = "Key Capability Configuration Image is Required")
    @Size(max = 50, message = "Key Capability Configuration Image must not exceed 500 characters")
    private String  keyCapabilityConfigurationImage;

    private Integer updatedById;

}
