package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCaseArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for UseCaseArtifact entity operations.
 */
@Repository
public interface UseCaseArtifactRepository extends JpaRepository<UseCaseArtifact, Integer> {

    List<UseCaseArtifact> findByUseCase_UsecaseId(Integer usecaseId);

    void deleteByUseCase_UsecaseId(Integer usecaseId);

    List<UseCaseArtifact> findAllByUseCase_UsecaseIdIn(List<Integer> ids);

    void deleteByUseCase_UsecaseIdAndArtifactTypeAndArtifactName(Long usecaseId, String artifactType, String artifactName);

}
