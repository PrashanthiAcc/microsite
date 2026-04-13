package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCaseTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for UseCaseTag entity operations.
 */
@Repository
public interface UseCaseTagRepository extends JpaRepository<UseCaseTag, Integer> {

    List<UseCaseTag> findByUseCase_UsecaseId(Integer usecaseId);

    void deleteByUseCase_UsecaseId(Integer usecaseId);

    List<UseCaseTag> findAllByUseCase_UsecaseIdIn(List<Integer> ids);
}
