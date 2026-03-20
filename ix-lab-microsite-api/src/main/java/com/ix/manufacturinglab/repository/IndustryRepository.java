package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * Repository for Industry entity operations.
 */
@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {
    List<Industry> findByIsActiveTrue();
}
