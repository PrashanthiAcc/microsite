package com.ix.manufacturinglab.service;

import com.ix.manufacturinglab.dto.HomePageRequestDTO;
import com.ix.manufacturinglab.dto.HomePageResponseDTO;
import org.springframework.web.multipart.MultipartFile;


public interface HomePageService {
    void createHomePage(HomePageRequestDTO requestDTO, MultipartFile heroImageUrl,MultipartFile keyCapConfigUrl, MultipartFile industryThumbnailUrl);

    HomePageResponseDTO getHomePage();
}