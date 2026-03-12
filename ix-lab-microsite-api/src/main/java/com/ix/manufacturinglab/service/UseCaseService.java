package com.ix.manufacturinglab.service;

import com.ix.manufacturinglab.dto.UseCaseRequestDTO;
import com.ix.manufacturinglab.dto.UseCaseResponseDTO;

import java.util.List;

/**
 * Service interface for Use Case CRUD operations.
 */
public interface UseCaseService {

    /**
     * Create a new use case and persist data across multiple entity tables.
     *
     * @param requestDTO the use case creation request
     * @return the created use case response
     */
    UseCaseResponseDTO createUseCase(UseCaseRequestDTO requestDTO);

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
    List<UseCaseResponseDTO> getAllActiveUseCases();

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
}
