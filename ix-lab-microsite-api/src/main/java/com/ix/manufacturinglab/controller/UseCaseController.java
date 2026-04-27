package com.ix.manufacturinglab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ix.common.exception.CommonErrorManagement;
import com.ix.manufacturinglab.constants.CommonExceptionConstants;
import com.ix.manufacturinglab.constants.ManufacturingLabConstants;
import com.ix.manufacturinglab.dto.FaqDTO;
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
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<Object> createUseCaseAndSaveAsDraft(@RequestBody UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_SAVING_DRAFT, requestDTO.getTitle());
        try {
            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
                errorResponse.setErrorDescription(ManufacturingLabConstants.DRAFT_TITLE_REQUIRED);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            UseCaseResponseDTO response = useCaseService.createUseCaseAndSaveAsDraft(requestDTO);
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
    public ResponseEntity<Object> createUseCaseAndsubmitForApproval(
            @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        //logger.debug(ManufacturingLabConstants.LOG_SUBMITTING_FOR_APPROVAL, requestDTO.getTitle());
        try {
            UseCaseResponseDTO response = useCaseService.createUseCaseAndSubmitForApproval(requestDTO);
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

        //logger.debug(ManufacturingLabConstants.LOG_FETCHING_USE_CASE, usecaseId);
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
     * @return list of use case responses
     */
    @GetMapping(value = "/v1/all")
    public ResponseEntity<Object> getAllUseCases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        //logger.info("Received request to fetch all use cases");
        try {
            Page<UseCaseResponseDTO> responses = useCaseService.getAllUseCases(page, size);
            return new ResponseEntity<>(responses, HttpStatus.OK);

        } catch (CommonException e) {
            logger.error("Exception occurred while fetching use cases: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_USE_CASE_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing use case and set status to DRAFT.
     *
     * @param usecaseId  the use case ID
     * @param requestDTO the update request body
     * @return the updated use case response
     */
    @PutMapping(value = "/v1/{usecaseId}/save-draft", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateUseCaseandSaveasDraft(@PathVariable("usecaseId") Integer usecaseId,
                                                              @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        logger.info(ManufacturingLabConstants.LOG_UPDATING_USE_CASE, usecaseId);
        try {
            UseCaseResponseDTO response = useCaseService.updateUseCaseandSaveasDraft(usecaseId, requestDTO);
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

    /**
     * Update an existing use case and set status to IN_REVIEW.
     *
     * @param usecaseId  the use case ID
     * @param requestDTO the update request body
     * @return the updated use case response
     */
    @PutMapping(value = "/v1/{usecaseId}/submit-for-approval", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateUseCaseandsubmitForApproval(@PathVariable("usecaseId") Integer usecaseId,
                                                                    @Valid @RequestBody UseCaseRequestDTO requestDTO) {

        //logger.debug("Submitting use case {} for approval", usecaseId);

        try {
            UseCaseResponseDTO response = useCaseService.updateUseCaseandsubmitForApproval(usecaseId, requestDTO);
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

        //logger.debug(ManufacturingLabConstants.LOG_DELETING_USE_CASE, usecaseId);
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
     * Update an existing use case with status as ARCHIVE.
     *
     * @param usecaseId the use case ID
     * @return the success text response
     */
    @PatchMapping("/v1/{usecaseId}/archive")
    public ResponseEntity<Object> archiveApprovedUseCase(@PathVariable Integer usecaseId) {

        //logger.debug("Received request to archive use case with id {}", usecaseId);

        try {
            useCaseService.archiveApprovedUseCase(usecaseId);
            return ResponseEntity.ok("Use case Id " + usecaseId + " is archived successfully");


        } catch (CommonException e) {

            logger.error("Exception occurred while archiving use case: {}", e.getMessage(), e);

            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(
                    ManufacturingLabConstants.ARCHIVE_USE_CASE_GENERIC_ERROR_MESSAGE);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Discard an existing APPROVED active use case.
     *
     * @param usecaseId the use case ID
     * @return the success text response
     */

    @DeleteMapping("/v1/{usecaseId}/discard")
    public ResponseEntity<Object> discardDraftUseCase(@PathVariable Integer usecaseId) {

        //logger.debug("Discarding draft use case with id {}", usecaseId);
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

    /**
     * Get all ARCHIVE use cases.
     *
     * @return list of ARCHIVE use case responses
     */

    @GetMapping(value = "/v1/archive/all")
    public ResponseEntity<Object> getAllArchiveUseCases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        //logger.debug("Received request to fetch all archive use cases");

        try {
            Page<UseCaseResponseDTO> responses = useCaseService.getAllArchiveUseCases(page, size);
            return new ResponseEntity<>(responses, HttpStatus.OK);

        } catch (CommonException e) {
            logger.error("Exception occurred while fetching use cases: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription(ManufacturingLabConstants.SEARCH_USE_CASE_GENERIC_ERROR_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update an existing use case and set status to APPROVED by super-admin.
     *
     * @param usecaseId  the use case ID
     * @param requestDTO the update request body
     * @return the updated use case response
     */
    @PutMapping("/v1/{usecaseId}/save")
    public ResponseEntity<String> updateUseCaseAndSaveBySuperAdmin(@PathVariable Integer usecaseId,
                                                                   @RequestBody UseCaseRequestDTO requestDTO) {

        //logger.debug("Submitting use case {} for approval", usecaseId);

        try {
            useCaseService.updateUseCaseBySuperAdmin(usecaseId, requestDTO);

            return ResponseEntity.ok("Use case updated successfully by Super Admin");

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

            return ResponseEntity.status(status).body(errorResponse.toString());
        }
    }

    @PostMapping(value = "/v1/save-draft-withblob", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createUseCaseAndSaveAsDraftWithBlob(
            @RequestPart("useCaseRequest") String requestJson,
            @RequestPart(value = "clientTestimonials", required = false) List<MultipartFile> clientTestimonials,
            @RequestPart(value = "demoVideos", required = false) List<MultipartFile> demoVideos,
            @RequestPart(value = "elevatorPitch", required = false) List<MultipartFile> elevatorPitch,
            @RequestPart(value = "userStory", required = false) List<MultipartFile> userStory,
            @RequestPart(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,
            @RequestPart(value = "bannerUrl", required = false) MultipartFile bannerUrl) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            UseCaseRequestDTO requestDTO =
                    mapper.readValue(requestJson, UseCaseRequestDTO.class);

            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
                errorResponse.setErrorDescription(ManufacturingLabConstants.DRAFT_TITLE_REQUIRED);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            if (requestDTO.getSpeakers() == null || requestDTO.getSpeakers().isEmpty()) {
                throw new CommonException(CommonExceptionConstants.BAD_REQUEST, "Speakers list cannot be empty");
            }

            UseCaseResponseDTO response =
                    useCaseService.createUseCaseAndSaveAsDraftWithBlob(requestDTO, clientTestimonials, demoVideos,thumbnailUrl, bannerUrl,elevatorPitch, userStory);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing request JSON", e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription("Invalid JSON format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping(value = "/v1/submit-for-approval-withblob", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createUseCaseAndsubmitForApprovalwithBlob(
            @RequestPart("useCaseRequest") String requestJson,
            @RequestPart(value = "clientTestimonials", required = false) List<MultipartFile> clientTestimonials,
            @RequestPart(value = "demoVideos", required = false) List<MultipartFile> demoVideos,
            @RequestPart(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,
            @RequestPart(value = "bannerUrl", required = false) MultipartFile bannerUrl,
            @RequestPart(value = "elevatorPitch", required = false) List<MultipartFile> elevatorPitch,
            @RequestPart(value = "userStory", required = false) List<MultipartFile> userStory) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            UseCaseRequestDTO requestDTO =
                    mapper.readValue(requestJson, UseCaseRequestDTO.class);

            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
                errorResponse.setErrorDescription(ManufacturingLabConstants.DRAFT_TITLE_REQUIRED);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            UseCaseResponseDTO response =
                    useCaseService.createUseCaseAndSubmitForApprovalwithBlob(requestDTO, clientTestimonials, demoVideos, thumbnailUrl, bannerUrl, elevatorPitch, userStory);

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing request JSON", e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription("Invalid JSON format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    }


    @PutMapping(value = "/v1/{usecaseId}/save-draft-withblob", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateUseCaseandSaveasDraftWithBlob(
            @PathVariable Integer usecaseId,
            @RequestPart("useCaseRequest") String requestJson,
            @RequestPart(value = "clientTestimonials", required = false) List<MultipartFile> clientTestimonials,
            @RequestPart(value = "demoVideos", required = false) List<MultipartFile> demoVideos,
            @RequestPart(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,
            @RequestPart(value = "bannerUrl", required = false) MultipartFile bannerUrl,
            @RequestPart(value = "elevatorPitch", required = false) List<MultipartFile> elevatorPitch,
            @RequestPart(value = "userStory", required = false) List<MultipartFile> userStory,
            @RequestParam(value = "clientTestimonialsUrls", required = false) List<String> clientTestimonialsUrls,
            @RequestParam(value = "demoVideosUrls", required = false) List<String> demoVideosUrls,
            @RequestParam(value = "elevatorPitchUrls", required = false) List<String> elevatorPitchUrls,
            @RequestParam(value = "userStoryUrls", required = false) List<String> userStoryUrls,
            @RequestPart(value = "thumbnailUrls", required = false) String thumbnailUrls,
            @RequestPart(value = "bannerUrls", required = false) String bannerUrls) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            UseCaseRequestDTO requestDTO = mapper.readValue(requestJson, UseCaseRequestDTO.class);
            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
                errorResponse.setErrorDescription(ManufacturingLabConstants.DRAFT_TITLE_REQUIRED);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            //UseCaseResponseDTO response = useCaseService.updateUseCaseandSaveasDraftWithBlob(usecaseId, requestDTO, clientTestimonials,demoVideos, thumbnailUrl, bannerUrl, elevatorPitch, userStory);
            UseCaseResponseDTO response =  useCaseService.updateUseCaseandSaveasDraftWithBlob(usecaseId, requestDTO,clientTestimonials,demoVideos,thumbnailUrl,bannerUrl, elevatorPitch, userStory, clientTestimonialsUrls, demoVideosUrls, elevatorPitchUrls, userStoryUrls, thumbnailUrls, bannerUrls);
            return ResponseEntity.ok(response);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing request JSON", e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription("Invalid JSON format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    }

    @PutMapping(value = "/v1/{usecaseId}/submit-for-approval-withblob", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> updateUseCaseAndSubmitForApprovalwithBlob(
            @PathVariable Integer usecaseId,
            @RequestPart("useCaseRequest") String requestJson,
            @RequestPart(value = "clientTestimonials", required = false) List<MultipartFile> clientTestimonials,
            @RequestPart(value = "demoVideos", required = false) List<MultipartFile> demoVideos,
            @RequestPart(value = "thumbnailUrl", required = false) MultipartFile thumbnailUrl,
            @RequestPart(value = "bannerUrl", required = false) MultipartFile bannerUrl,
            @RequestPart(value = "elevatorPitch", required = false) List<MultipartFile> elevatorPitch,
            @RequestPart(value = "userStory", required = false) List<MultipartFile> userStory,
            @RequestParam(value = "clientTestimonialsUrls", required = false) List<String> clientTestimonialsUrls,
            @RequestParam(value = "demoVideosUrls", required = false) List<String> demoVideosUrls,
            @RequestParam(value = "elevatorPitchUrls", required = false) List<String> elevatorPitchUrls,
            @RequestParam(value = "userStoryUrls", required = false) List<String> userStoryUrls,
            @RequestParam(value = "thumbnailUrls", required = false) String thumbnailUrls,
            @RequestParam(value = "bannerUrls", required = false) String bannerUrls) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            UseCaseRequestDTO requestDTO = mapper.readValue(requestJson, UseCaseRequestDTO.class);
            if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
                errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
                errorResponse.setErrorDescription(ManufacturingLabConstants.DRAFT_TITLE_REQUIRED);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            UseCaseResponseDTO response = useCaseService.updateUseCaseAndSubmitForApprovalwithBlob(usecaseId, requestDTO, clientTestimonials,
                    demoVideos, thumbnailUrl, bannerUrl , elevatorPitch, userStory, clientTestimonialsUrls, demoVideosUrls, elevatorPitchUrls, userStoryUrls, thumbnailUrls, bannerUrls);
            return ResponseEntity.ok(response);

        } catch (CommonException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing request JSON", e);
            errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
            errorResponse.setErrorDescription("Invalid JSON format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    }

    @PostMapping(value = "/v1/upload-demo-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadDemoVideo(@RequestPart("demoVideo") MultipartFile demoVideo) {
        logger.info("Received request to upload demo video");

        try {
            // Validate file
            if (demoVideo == null || demoVideo.isEmpty()) {
                errorResponse.setErrorCode(CommonExceptionConstants.BAD_REQUEST);
                errorResponse.setErrorDescription("Demo video file is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Call service method
            String sasUrl = useCaseService.uploadDemoVideoInCheckMode(demoVideo);

            // Build response
            Map<String, String> response = new HashMap<>();
            response.put("message", "Demo video uploaded successfully");
            response.put("sasUrl", sasUrl);

            return ResponseEntity.ok(response);

        } catch (CommonException e) {
            logger.error("Error occurred while uploading demo video: {}", e.getMessage(), e);
            errorResponse.setErrorCode(e.getErrorCode());
            errorResponse.setErrorDescription(e.getErrorDescription());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while uploading demo video: {}", e.getMessage(), e);
            errorResponse.setErrorCode(CommonExceptionConstants.INTERNAL_SERVER_ERROR);
            errorResponse.setErrorDescription("An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/v1/count")
    public ResponseEntity<Map<String, Object>> getApprovedUseCaseCount() {

        Map<String, Object> response = useCaseService.getApprovedActiveUseCaseCount();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/usecases/count-by-industry")
    public ResponseEntity<List<Map<String, Object>>> getUseCaseCountByIndustry() {

        return ResponseEntity.ok(useCaseService.getUseCaseCountByIndustry());
    }

    @PutMapping("/v1/{usecaseId}/faqs")
    public ResponseEntity<Map<String,Object>> saveFaqs(@PathVariable Integer usecaseId,
                                                       @RequestBody List<FaqDTO> faqDTOs) {

        useCaseService.saveFaqs(usecaseId, faqDTOs);

        return ResponseEntity.ok(
                Map.of(
                        "status",200,
                        "message","FAQs Saved successfully"
                )
        );
    }

    @GetMapping("/v1/{usecaseId}/faqs")
    public ResponseEntity<List<FaqDTO>> getUseCaseFaqs(@PathVariable Integer usecaseId) {

        return ResponseEntity.ok(useCaseService.getFaqsByUseCaseId(usecaseId));
    }

}
