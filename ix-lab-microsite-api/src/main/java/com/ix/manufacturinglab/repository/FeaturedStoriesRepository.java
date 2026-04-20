package com.ix.manufacturinglab.repository;


import com.ix.manufacturinglab.entity.FeaturedStories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedStoriesRepository extends JpaRepository<FeaturedStories, Long> {

    List<FeaturedStories> findAll();
}
