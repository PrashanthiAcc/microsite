package com.ix.manufacturinglab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndustryThumbnailDTO {
    private Long industryThumbnailId;
    private Long industryId;
    private String industryThumbnailUrl;
}
