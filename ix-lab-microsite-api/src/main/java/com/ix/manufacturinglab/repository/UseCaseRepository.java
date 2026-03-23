package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCase;
import com.ix.manufacturinglab.enums.UseCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Page<UseCase> findByIsActiveTrue(Pageable pageable);

    Optional<UseCase> findByUsecaseIdAndStatus(Integer usecaseId, String status);

    Optional<UseCase> findByParentUsecaseIdAndStatus(Integer parentUsecaseId, String status);
    Page<UseCase> findByStatus(String status, Pageable pageable);
}
