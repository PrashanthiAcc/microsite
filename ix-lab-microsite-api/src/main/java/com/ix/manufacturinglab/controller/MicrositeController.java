package com.ix.manufacturinglab.controller;

import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.common.exception.CommonErrorManagement;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.dto.IndustryDTO;
import com.ix.manufacturinglab.dto.MicrositeDataDTO;
import com.ix.manufacturinglab.dto.SubIndustryDTO;
import com.ix.manufacturinglab.dto.ValueChainDTO;
import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.entity.SubIndustry;
import com.ix.manufacturinglab.entity.ValueChain;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.service.MicrositeService;
import com.ix.manufacturinglab.service.UseCaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Microsite data APIs.
 * Provides GET endpoints to retrieve Industry, SubIndustry, and ValueChain static data.
 */
@RestController
@RequestMapping(value = "/industry", produces = MediaType.APPLICATION_JSON_VALUE)
public class MicrositeController {

    private static final Logger logger = LoggerFactory.getLogger(MicrositeController.class);
    private final MicrositeService micrositeService;
    private final CommonErrorManagement errorResponse;

    @Autowired
    public MicrositeController(MicrositeService micrositeService, com.ix.common.exception.CommonErrorManagement errorResponse) {
        this.micrositeService = micrositeService;
        this.errorResponse = errorResponse;
    }

    // ==================== INDUSTRY ENDPOINTS ====================

    /**
     * Get all industries.
     *
     * @return list of all industries
     */
    @GetMapping(value = "/v1/microsite/industries")
    public ResponseEntity<Object> getAllIndustries() {

        logger.info("Received request to fetch all industries");
        try {
            List<Industry> industries = micrositeService.getAllIndustries();
            return new ResponseEntity<>(industries, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetch all Industries: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_INDUSTRY_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== SUB-INDUSTRY ENDPOINTS ====================

    /**
     * Get all sub-industries.
     *
     * @return list of all sub-industries
     */
    @GetMapping(value = "/v1/microsite/sub-industries")
    public ResponseEntity<Object> getAllSubIndustries() {

        logger.info("Received request to fetch all sub-industries");
        try {
            List<SubIndustry> subIndustries = micrositeService.getAllSubIndustries();
            return new ResponseEntity<>(subIndustries, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetch Sub Industries: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get sub-industries by industry ID.
     *
     * @param industryId    the industry ID
     * @return list of sub-industries for the given industry
     */
    @GetMapping(value = "/v1/microsite/industries/{industryId}/sub-industries")
    public ResponseEntity<Object> getSubIndustriesByIndustryId(
            @PathVariable("industryId") Long industryId) {

        logger.info("Received request to fetch sub-industries for industryId: {}", industryId);
        try {
            List<SubIndustry> subIndustries = micrositeService.getSubIndustriesByIndustryId(industryId);
            return new ResponseEntity<>(subIndustries, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetch Sub Industries: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== VALUE CHAIN ENDPOINTS ====================

    /**
     * Get all value chains.
     *
     * @return list of all value chains
     */
    @GetMapping(value = "/v1/microsite/value-chains")
    public ResponseEntity<Object> getAllValueChains() {

        logger.info("Received request to fetch all value chains");
        try {
            List<ValueChain> valueChains = micrositeService.getAllValueChains();
            return new ResponseEntity<>(valueChains, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetching all value chain: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_VALUE_CHAIN_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get value chains filtered by industry ID and/or sub-industry ID.
     *
     * @param industryId    the industry ID (optional)
     * @param subIndustryId the sub-industry ID (optional)
     * @return list of value chains matching the criteria
     */
    @GetMapping(value = "/v1/microsite/value-chains/filter")
    public ResponseEntity<Object> getValueChainsByFilter(
            @RequestParam(name = "industryId", required = false) Long industryId,
            @RequestParam(name = "subIndustryId", required = false) Long subIndustryId) {

        logger.info("Received request to fetch value chains for industryId: {} and subIndustryId: {}", industryId, subIndustryId);
        try {
            List<ValueChain> valueChains = micrositeService.getValueChainsByIndustryAndSubIndustry(industryId, subIndustryId);
            return new ResponseEntity<>(valueChains, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetching all value chain by industry id and sub industry id: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_VALUE_CHAIN_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== AGGREGATED DATA ENDPOINT ====================

    /**
     * Get all microsite data (industries, sub-industries, value chains) in a single response.
     *
     * @return aggregated microsite data
     */
    @GetMapping(value = "/v1/all/data")
    public ResponseEntity<Object> getAllMicrositeData() {

        logger.info("Received request to fetch all microsite data");
        try {
            MicrositeDataDTO micrositeData = micrositeService.getAllMicrositeData();
            return new ResponseEntity<>(micrositeData, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetching microsite data: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_INDUSTRY_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
