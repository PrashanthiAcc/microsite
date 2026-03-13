package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Repository for UseCase entity operations.
 */
@Repository
public interface UseCaseRepository extends JpaRepository<UseCase, Integer> {

    Page<UseCase> findByIsActiveTrue(Pageable pageable);
}
