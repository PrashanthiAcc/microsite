package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCaseSpeaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for UseCaseSpeaker entity operations.
 */
@Repository
public interface UseCaseSpeakerRepository extends JpaRepository<UseCaseSpeaker, Integer> {

    List<UseCaseSpeaker> findByUseCase_UsecaseId(Integer usecaseId);

    void deleteByUseCase_UsecaseId(Integer usecaseId);
}
