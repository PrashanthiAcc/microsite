package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCase;
import com.ix.manufacturinglab.enums.UseCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UseCase entity operations.
 */
@Repository
public interface UseCaseRepository extends JpaRepository<UseCase, Integer> {

    Page<UseCase> findAll(Pageable pageable);

    Optional<UseCase> findByUsecaseIdAndStatus(Integer usecaseId, String status);

    Optional<UseCase> findByParentUsecaseIdAndStatus(Integer parentUsecaseId, String status);
    Page<UseCase> findByStatus(String status, Pageable pageable);

    @Modifying
    @Query("UPDATE UseCase u SET u.status = 'ARCHIVED' WHERE u.ownerEid = :ownerEid")
    void archiveUseCasesByOwnerEid(@Param("ownerEid") String ownerEid);

    long countByStatusAndIsActive(String status, boolean isActive);

    @Query(value = """
        SELECT v.industry_id,
               COUNT(u.usecase_id) AS usecase_count
        FROM mfg.usecase u
        JOIN mfg.value_chain v
            ON u.value_chain_id = v.value_chain_id
        WHERE u.status = 'APPROVED'
          AND u.is_active = 'true'
        GROUP BY v.industry_id
        """, nativeQuery = true)
    List<Object[]> getApprovedActiveUseCaseCountByIndustry();

    Page<UseCase> findByStatusAndIsActiveTrue(String status, Pageable pageable);

    List<UseCase> findByUsecaseIdIn(List<Integer> ids);

}
