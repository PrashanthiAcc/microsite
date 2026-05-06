package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.Favourite;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FavouriteUseCaseRepository extends JpaRepository<Favourite, Integer> {
    Page<Favourite> findByUserId(Integer userId, Pageable pageable);

    Optional<Favourite> findByUserIdAndUsecaseId(Integer userId, Integer usecaseId);

}
