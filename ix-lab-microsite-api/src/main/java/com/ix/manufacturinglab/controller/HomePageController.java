package com.ix.manufacturinglab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.dto.HomePageRequestDTO;
import com.ix.manufacturinglab.dto.HomePageResponseDTO;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.service.HomePageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ix.common.exception.CommonErrorManagement;

import java.util.List;



@RestController
@RequestMapping(value = "/api/homepage", produces = MediaType.APPLICATION_JSON_VALUE)
public class HomePageController {

    private static final Logger logger = LoggerFactory.getLogger(HomePageController.class);
    private final HomePageService homePageService;
    private final CommonErrorManagement errorResponse;

    @Autowired
    public HomePageController(HomePageService homePageService,
                              CommonErrorManagement errorResponse) {
        this.homePageService = homePageService;
        this.errorResponse = errorResponse;
    }

    @PostMapping(value = "/v1/home-page-configuration",consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<Object> create(@RequestPart("homePageRequest") String  requestJson,
                                         @RequestPart(value = "heroImageUrl", required = false) MultipartFile heroImageUrl,
                                         @RequestPart(value = "keyCapConfigUrl", required = false) MultipartFile keyCapConfigUrl,
                                         @RequestPart(value = "industrythumbnailUrl", required = false) List<MultipartFile> industryThumbnailUrl) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            HomePageRequestDTO requestDTO = mapper.readValue(requestJson, HomePageRequestDTO.class);
            homePageService.createHomePage(requestDTO, heroImageUrl, keyCapConfigUrl, industryThumbnailUrl);
            return ResponseEntity.ok("Created successfully");

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error while creating Home Page Configuration", e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription("Failed to initialize Home Page Configuration");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/v1/home-page-configuration")
    public ResponseEntity<HomePageResponseDTO> get() {

        HomePageResponseDTO response = homePageService.getHomePage();

        return ResponseEntity.ok(response);
    }

}
