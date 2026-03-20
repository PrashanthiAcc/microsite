package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.ValueChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ValueChain entity operations.
 */
@Repository
public interface ValueChainRepository extends JpaRepository<ValueChain, Long> {

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

    boolean existsBySubIndustryId(Long subIndustryId);
}
