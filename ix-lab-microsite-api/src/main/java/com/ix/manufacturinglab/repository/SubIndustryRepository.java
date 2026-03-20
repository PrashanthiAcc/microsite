package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.SubIndustry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SubIndustry entity operations.
 */
@Repository
public interface SubIndustryRepository extends JpaRepository<SubIndustry, Long> {

    /**
     * Fetch all sub-industries whose parent industry is ACTIVE.
     */
    @Query("SELECT si FROM SubIndustry si JOIN Industry i ON si.industryId = i.industryId WHERE i.isActive = true")
    List<SubIndustry> findAllByActiveIndustry();

    /**
     * Fetch sub-industries for a given industry ID,
     * only if the parent industry is ACTIVE.
     */
    @Query("SELECT si FROM SubIndustry si JOIN Industry i ON si.industryId = i.industryId WHERE i.industryId = :industryId AND i.isActive = true")
    List<SubIndustry> findActiveSubIndustriesByIndustryId(Long industryId);

}
