package com.ix.manufacturinglab.service;

import com.ix.manufacturinglab.dto.MicrositeDataDTO;
import com.ix.manufacturinglab.entity.Industry;
import com.ix.manufacturinglab.entity.SubIndustry;
import com.ix.manufacturinglab.entity.ValueChain;

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
