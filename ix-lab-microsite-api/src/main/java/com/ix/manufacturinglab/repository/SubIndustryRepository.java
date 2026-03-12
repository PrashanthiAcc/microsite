package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.SubIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SubIndustry entity operations.
 */
@Repository
public interface SubIndustryRepository extends JpaRepository<SubIndustry, Long> {

    /**
     * Find all sub-industries for a given industry.
     */
    List<SubIndustry> findByIndustryId(Long industryId);
}
