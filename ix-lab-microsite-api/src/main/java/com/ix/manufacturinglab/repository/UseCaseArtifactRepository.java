package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCaseArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
    @Transactional
    @Query("DELETE FROM UseCaseArtifact u WHERE u.useCase.usecaseId = :usecaseId AND u.artifactType = :artifactType")
    int deleteByUseCase_UsecaseIdAndArtifactType(@Param("usecaseId") Long usecaseId, @Param("artifactType") String artifactType);

}
