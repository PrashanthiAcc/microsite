package com.ix.manufacturinglab.repository;


import com.ix.manufacturinglab.entity.IndustryThumbnails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndustryThumbnailsRepository extends JpaRepository<IndustryThumbnails, Long> {

    List<IndustryThumbnails> findAll();
}
