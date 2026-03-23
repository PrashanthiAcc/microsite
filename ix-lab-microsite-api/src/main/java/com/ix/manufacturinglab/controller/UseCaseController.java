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
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Use Case CRUD operations.
 * Handles creation, retrieval, update, and soft-delete of use cases
 * with data distributed across multiple entity tables.
 */
@RestController
@RequestMapping(value = "/api/usecase", produces = MediaType.APPLICATION_JSON_VALUE)
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
     * Save a use case as draft (minimal validation).
     * Only title is required. Status is automatically set to DRAFT.
     *
     * @param requestDTO the use case request body
     * @return the saved draft use case response
     */
    @PostMapping(value = "/v1/save-draft", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> saveAsDraft(@RequestBody UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());
        try {
            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
                errorResponse.setErrorDescription(ManufacturingLabConstants.DRAFT_TITLE_REQUIRED);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            UseCaseResponseDTO response = useCaseService.saveAsDraft(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (CommonException e) {
            logger.error("Exception occurred while saving use case as draft: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SAVE_DRAFT_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Submit a use case for approval (full validation).
     * Status is automatically set to IN_REVIEW.
     *
     * @param requestDTO the use case request body
     * @return the submitted use case response
     */
    @PostMapping(value = "/v1/submit-for-approval", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> submitForApproval(
            @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_SUBMITTING_FOR_APPROVAL, requestDTO.getTitle());
        try {
            UseCaseResponseDTO response = useCaseService.submitForApproval(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (CommonException e) {
            logger.error("Exception occurred while submitting use case for approval: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SUBMIT_FOR_APPROVAL_GENERIC_ERROR_MESSAGE);
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
    public ResponseEntity<Object> getAllActiveUseCases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Received request to fetch all active use cases");

        try {
            Page<UseCaseResponseDTO> responses = useCaseService.getAllActiveUseCases(page, size);
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
    @PutMapping(value = "/v1/{usecaseId}/updateUsecaseByAdmin/draft", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateUseCase(@PathVariable("usecaseId") Integer usecaseId,
                                                @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);
        try {
            UseCaseResponseDTO response = useCaseService.updateUseCase(usecaseId, requestDTO);
            return ResponseEntity.status(HttpStatus.OK).body("Use case Id " + usecaseId + " is updated successfully");
        } catch (CommonException e) {
            logger.error("Exception occurred while updating use case: {}", e.getMessage(), e);

            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());

            HttpStatus status;

            try {
                status = HttpStatus.valueOf(Integer.parseInt(e.getErrorCode()));
            } catch (Exception ex) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @PutMapping(value = "/v1/{usecaseId}/updateUsecaseByAdmin/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> submitForApproval(@PathVariable("usecaseId") Integer usecaseId,
                                                    @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        logger.info("Submitting use case {} for approval", usecaseId);

        try {
            UseCaseResponseDTO response = useCaseService.updatesubmitForApproval(usecaseId, requestDTO);
            return ResponseEntity.ok("Use case submitted for approval successfully");

        } catch (CommonException e) {
            logger.error("Exception occurred while updating use case: {}", e.getMessage(), e);

            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());

            HttpStatus status;

            try {
                status = HttpStatus.valueOf(Integer.parseInt(e.getErrorCode()));
            } catch (Exception ex) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

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
            return ResponseEntity.status(HttpStatus.OK).body("Use case Id " + usecaseId + " is deleted successfully");
        } catch (CommonException e) {
            logger.error("Exception occurred while deleting use case: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());
            HttpStatus status = CommonExceptionConstants.NOT_FOUND.equals(e.getErrorCode())
                    ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    /**
     * Update an existing use case.
     *
     * @param usecaseId the use case ID
     * @return the success text response
     */
    @PatchMapping("/v1/{usecaseId}/archive")
    public ResponseEntity<Object> archiveUseCase(@PathVariable Integer usecaseId) {

        logger.info("Received request to archive use case with id {}", usecaseId);

        try {
            useCaseService.archiveUseCase(usecaseId);
            return ResponseEntity.ok("Use case Id " + usecaseId + " is archived successfully");


        } catch (CommonException e) {

            logger.error("Exception occurred while archiving use case: {}", e.getMessage(), e);

            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(
                    ManufacturingLabConstants.ARCHIVE_USE_CASE_GENERIC_ERROR_MESSAGE);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/v1/{usecaseId}/discard")
    public ResponseEntity<Object> discardDraftUseCase(@PathVariable Integer usecaseId) {

        logger.info("Discarding draft use case with id {}", usecaseId);
        try {
            useCaseService.discardDraftUseCase(usecaseId);

            return ResponseEntity.ok(
                    "Draft use case with Id " + usecaseId + " is discarded successfully");

        } catch (CommonException e) {

            logger.error("Exception occurred while archiving use case: {}", e.getMessage(), e);

            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(
                    ManufacturingLabConstants.DISCARD_USE_CASE_GENERIC_ERROR_MESSAGE);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

}
