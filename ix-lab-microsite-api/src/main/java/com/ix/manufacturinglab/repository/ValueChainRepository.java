package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.ValueChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ValueChain entity operations.
 */
@Repository
public interface ValueChainRepository extends JpaRepository<ValueChain, Long> {

    /**
     * Fetch all value chains whose parent industry is ACTIVE.
     */
    @Query("SELECT vc FROM ValueChain vc JOIN Industry i ON vc.industryId = i.industryId WHERE i.isActive = true")
    List<ValueChain> findAllByActiveIndustry();

    /**
     * Find all value chains for a given industry.
     */
    List<ValueChain> findByIndustryId(Long industryId);

    /**
     * Find all value chains for a given sub-industry.
     */
    List<ValueChain> findBySubIndustryId(Long subIndustryId);

    /**
     * Find all value chains for a given industry and sub-industry.
     */
    List<ValueChain> findByIndustryIdAndSubIndustryId(Long industryId, Long subIndustryId);

    /**
     * Fetch value chains filtered by industry ID and/or sub-industry ID,
     * only if the parent industry is ACTIVE.
     * If industryId or subIndustryId is null, that filter is ignored.
     */
    @Query("SELECT vc FROM ValueChain vc JOIN Industry i ON vc.industryId = i.industryId WHERE i.isActive = true AND (:industryId IS NULL OR vc.industryId = :industryId) AND (:subIndustryId IS NULL OR vc.subIndustryId = :subIndustryId)")
    List<ValueChain> findFilteredValueChains(Long industryId, Long subIndustryId);
}
