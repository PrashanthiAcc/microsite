package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteUseCaseRepository extends JpaRepository<Favourite, Integer> {
    List<Favourite> findByUserId(Integer userId);

    Optional<Favourite> findByUserIdAndUsecaseId(Integer userId, Integer usecaseId);

}
