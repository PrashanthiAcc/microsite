package com.ix.manufacturinglab.service;

import com.ix.manufacturinglab.dto.UseCaseRequestDTO;
import com.ix.manufacturinglab.dto.UseCaseResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for Use Case CRUD operations.
 */
public interface UseCaseService {

    /**
     * Saves a new use case as a draft and persists data across multiple related tables.
     *
     * @param requestDTO the use case draft request data
     * @return the saved use case response containing draft details
     */
    UseCaseResponseDTO saveAsDraft(UseCaseRequestDTO requestDTO);

    /**
     * Submits a use case for approval and persists data across multiple related tables.
     * updates the status from to IN_REVIEW.
     *
     * @param requestDTO the use case request data to be submitted for approval
     * @return the submitted use case response containing approval details
     */
    UseCaseResponseDTO submitForApproval(UseCaseRequestDTO requestDTO);

    /**
     * Discards a draft use case by deleting it from all related tables based on the given use case ID.
     * This operation is allowed only if the use case is in DRAFT status.
     *
     * @param usecaseId the ID of the draft use case to be discarded
     */

    void discardDraftUseCase (Integer usecaseId);

    /**
     * Get a use case by its ID with all related data.
     *
     * @param usecaseId the use case ID
     * @return the use case response
     */
    UseCaseResponseDTO getUseCaseById(Integer usecaseId);

    /**
     * Get all active use cases.
     *
     * @return list of active use case responses
     */
    Page<UseCaseResponseDTO> getAllActiveUseCases(int page, int size);

    /**
     * Update an existing use case and its related data.
     *
     * @param usecaseId  the use case ID
     * @param requestDTO the update request
     * @return the updated use case response
     */
    UseCaseResponseDTO updateUseCase(Integer usecaseId, UseCaseRequestDTO requestDTO);

    /**
     * Soft-delete a use case (set is_active = false).
     *
     * @param usecaseId the use case ID
     */
    void deleteUseCase(Integer usecaseId);

    /**
     * Archives an existing use case.
     *
     * @param usecaseId  the use case ID
     * @return the success message
     */
    void archiveUseCase(Integer usecaseId);

}
