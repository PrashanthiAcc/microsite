package com.ix.manufacturinglab.service;

import com.ix.manufacturinglab.dto.MicrositeDataDTO;
import com.ix.manufacturinglab.dto.SubIndustryDTO;
import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.entity.SubIndustry;
import com.ix.manufacturinglab.entity.ValueChain;
import com.ix.manufacturinglab.exception.CommonException;

import java.util.List;

/**
 * Service interface for Microsite data operations (Industry, SubIndustry, ValueChain).
 */
public interface MicrositeService {

    /**
     * Get all industries.
     *
     * @return list of all industries
     */
    List<Industry> getAllIndustries();

    /**
     * Create a new industry.
     *
     * @param industry industry details to be created
     * @return created industry
     * @throws CommonException if creation fails
     */
    Industry createIndustry(Industry industry) throws CommonException;

    /**
     * Update an existing industry.
     *
     * @param id industry id
     * @param industry updated industry details
     * @return updated industry
     * @throws CommonException if industry not found or update fails
     */
    Industry updateIndustry(Long id, Industry industry) throws CommonException;

    /**
     * Delete an industry by id.
     *
     * @param id industry id
     * @throws CommonException if industry not found or delete fails
     */
    void deleteIndustry(Long id) throws CommonException;

    /**
     * Get all sub-industries.
     *
     * @return list of all sub-industries
     */
    List<SubIndustry> getAllSubIndustries();

    /**
     * Get sub-industries by industry ID.
     *
     * @param industryId the industry ID
     * @return list of sub-industries for the given industry
     */
    List<SubIndustry> getSubIndustriesByIndustryId(Long industryId);

    /**
     * Create a new sub-industry
     *
     * @param subIndustryDTO the su-industry creation request
     * @return the created sub-industry response
     */

    SubIndustryDTO createSubIndustry(SubIndustryDTO subIndustryDTO);

    /**
     * Update an existing sub-industryand its related data.
     *
     * @param subIndustryId  the sub-industry ID
     * @param subIndustryDTO the update request
     * @return the updated sub-industry response
     */

    SubIndustryDTO updateSubIndustry(Long subIndustryId, SubIndustryDTO subIndustryDTO);

    /**
     * Hard-delete a sub_industry.
     *
     * @param subIndustryId the sub-industry ID
     */

    void deleteSubIndustry(Long subIndustryId);

    /**
     * Get all value chains.
     *
     * @return list of all value chains
     */
    List<ValueChain> getAllValueChains();

    /**
     * Get value chains by industry and sub-industry IDs.
     *
     * @param industryId    the industry ID
     * @param subIndustryId the sub-industry ID
     * @return list of value chains matching the criteria
     */
    List<ValueChain> getValueChainsByIndustryAndSubIndustry(Long industryId, Long subIndustryId);

    /**
     * Get aggregated microsite data (all industries, sub-industries, and value chains).
     *
     * @return aggregated microsite data
     */
    MicrositeDataDTO getAllMicrositeData();
}
