package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCaseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UseCaseContent entity operations.
 */
@Repository
public interface UseCaseContentRepository extends JpaRepository<UseCaseContent, Integer> {

    Optional<UseCaseContent> findByUsecaseId(Integer usecaseId);

    void deleteByUsecaseId(Integer usecaseId);
    List<UseCaseContent> findAllByUsecaseIdIn(List<Integer> ids);
}
