package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UseCase;
import com.ix.manufacturinglab.entity.UseCaseFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UseCaseFaqRepository extends JpaRepository<UseCaseFaq, Integer> {

    List<UseCaseFaq> findAllByUseCase_UsecaseIdIn(List<Integer> ids);

    List<UseCaseFaq> findByUseCase(UseCase useCase);

    List<UseCaseFaq> findByUseCaseUsecaseId(Integer usecaseId);
}
