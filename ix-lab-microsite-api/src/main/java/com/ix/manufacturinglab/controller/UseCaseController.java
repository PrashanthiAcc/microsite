package com.ix.manufacturinglab.controller;

import com.ix.common.exception.CommonErrorManagement;
import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.dto.UseCaseRequestDTO;
import com.ix.manufacturinglab.dto.UseCaseResponseDTO;
import com.ix.manufacturinglab.exception.CommonException;
import com.ix.manufacturinglab.service.UseCaseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Use Case CRUD operations.
 * Handles creation, retrieval, update, and soft-delete of use cases
 * with data distributed across multiple entity tables.
 */
@RestController
@RequestMapping(value = "/usecase", produces = MediaType.APPLICATION_JSON_VALUE)
public class UseCaseController {

    private static final Logger logger = LoggerFactory.getLogger(UseCaseController.class);
    private final UseCaseService useCaseService;
    private final CommonErrorManagement errorResponse;

    @Autowired
    public UseCaseController(UseCaseService useCaseService, CommonErrorManagement errorResponse) {
        this.useCaseService = useCaseService;
        this.errorResponse = errorResponse;
    }

    /**
     * Create a new use case.
     *
     * @param requestDTO the use case request body
     * @return the created use case response
     */
    @PostMapping(value = "/v1/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createUseCase(
            @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_CREATING_USE_CASE, requestDTO.getTitle());
        try {
            UseCaseResponseDTO response = useCaseService.createUseCase(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (CommonException e) {
            logger.error("Exception occurred while creating use case: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.CREATE_USE_CASE_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get a use case by ID.
     *
     * @param usecaseId the use case ID
     * @return the use case response
     */
    @GetMapping(value = "/v1/{usecaseId}")
    public ResponseEntity<Object> getUseCaseById(@PathVariable("usecaseId") Integer usecaseId) {

        logger.info(ManufacturingLabConstants.LOG_FETCHING_USE_CASE, usecaseId);
        try {
            UseCaseResponseDTO response = useCaseService.getUseCaseById(usecaseId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetching use case: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all active use cases.
     *
     * @return list of active use case responses
     */
    @GetMapping(value = "/v1/all")
    public ResponseEntity<Object> getAllActiveUseCases() {

        logger.info("Received request to fetch all active use cases");
        try {
            List<UseCaseResponseDTO> responses = useCaseService.getAllActiveUseCases();
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while fetching use cases: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_USE_CASE_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing use case.
     *
     * @param usecaseId  the use case ID
     * @param requestDTO the update request body
     * @return the updated use case response
     */
    @PutMapping(value = "/v1/{usecaseId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateUseCase(
            @PathVariable("usecaseId") Integer usecaseId,
            @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);
        try {
            UseCaseResponseDTO response = useCaseService.updateUseCase(usecaseId, requestDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CommonException e) {
            logger.error("Exception occurred while updating use case: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());
            HttpStatus status = CommonExceptionConstants.NOT_FOUND.equals(e.getErrorCode())
                    ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    /**
     * Soft-delete a use case (set is_active = false).
     *
     * @param usecaseId the use case ID
     * @return no content on success
     */
    @DeleteMapping(value = "/v1/{usecaseId}")
    public ResponseEntity<Object> deleteUseCase(@PathVariable("usecaseId") Integer usecaseId) {

        logger.info(ManufacturingLabConstants.LOG_DELETING_USE_CASE, usecaseId);
        try {
            useCaseService.deleteUseCase(usecaseId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (CommonException e) {
            logger.error("Exception occurred while deleting use case: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());
            HttpStatus status = CommonExceptionConstants.NOT_FOUND.equals(e.getErrorCode())
                    ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}
