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

    /**
     * Create a new industry.
     *
     * @param industry industry details
     * @return created industry
     */
    @PostMapping(value = "/v1/microsite/create")
    public ResponseEntity<Object> createIndustry(@RequestBody Industry industry) {

        logger.info("Received request to create industry");
        try {
            micrositeService.createIndustry(industry);
            return new ResponseEntity<>(ManufacturingLabConstants.INDUSTRY_CREATED_SUCCESS, HttpStatus.CREATED);
        } catch (CommonException e) {
            logger.error("Exception occurred while creating industry: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.CREATE_INDUSTRY_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing industry.
     *
     * @param id industry id
     * @param industry updated industry details
     * @return updated industry
     */
    @PutMapping("/v1/microsite/industries/{id}")
    public ResponseEntity<Object> updateIndustry(@PathVariable Long id,
                                                 @RequestBody Industry industry) {

        logger.info("Received request to update industry with id {}", id);
        try {
            Industry updatedIndustry = micrositeService.updateIndustry(id, industry);
            return new ResponseEntity<>(updatedIndustry, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while deleting industry with id {}: {}", id, e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete an industry by id.
     *
     * @param id industry id
     * @return success message
     */
    @DeleteMapping("/v1/microsite/industries/{id}")
    public ResponseEntity<Object> deleteIndustry(@PathVariable Long id) {

        logger.info("Received request to delete industry with id {}", id);
        try {
            micrositeService.deleteIndustry(id);
            return new ResponseEntity<>(ManufacturingLabConstants.INDUSTRY_DELETED_SUCCESS, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while deleting industry with id {}: {}", id, e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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

    /**
     * Create a new sub-industry.
     *
     * @param subIndustryDTO the sub-industry request body
     * @return the success response
     */
    @PostMapping(value = "/v1/microsite/sub-industries/create")
    public ResponseEntity<Object> createSubIndustry(@RequestBody SubIndustryDTO subIndustryDTO) {

        logger.info("Received request to create sub-industry");

        try {
            SubIndustryDTO response = micrositeService.createSubIndustry(subIndustryDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Sub-industry created successfully");

        } catch (CommonException e) {

            logger.error("Exception occurred while creating Sub Industry: {}", e.getMessage(), e);

            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(
                    ManufacturingLabConstants.CREATE_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing sub-industry.
     *
     * @param subIndustryId  the sub-industry ID
     * @param subIndustryDTO the update request body
     * @return the updated sub-industry response
     */
    @PutMapping("/v1/microsite/sub-industries/{subIndustryId}")
    public ResponseEntity<Object> updateSubIndustry(@PathVariable Long subIndustryId,
                                                    @RequestBody SubIndustryDTO subIndustryDTO) {
        logger.info("Received request to update sub-industry");
        try {
            SubIndustryDTO response = micrositeService.updateSubIndustry(subIndustryId, subIndustryDTO);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (CommonException e) {

            logger.error("Exception occurred while updating Sub Industry: {}", e.getMessage(), e);

            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(
                    ManufacturingLabConstants.UPDATE_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Hard-delete a sub-industry
     *
     * @param subIndustryId the sub-industry ID
     * @return success response
     */
    @DeleteMapping("/v1/microsite/sub-industries/{subIndustryId}")
    public ResponseEntity<Object> deleteSubIndustry(@PathVariable Long subIndustryId) {

        logger.info("Received request to delete sub-industry with id {}", subIndustryId);
        try {

            micrositeService.deleteSubIndustry(subIndustryId);

            return ResponseEntity.ok("Sub-industry deleted successfully");

        } catch (CommonException e) {

            logger.error("Exception occurred while deleting Sub Industry: {}", e.getMessage(), e);

            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(
                    ManufacturingLabConstants.DELETE_SUB_INDUSTRY_GENERIC_ERROR_MESSAGE);

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
