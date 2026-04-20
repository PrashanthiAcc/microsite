package com.ix.manufacturinglab.repository;


import com.ix.manufacturinglab.entity.HomePageConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HomePageConfigurationRepository extends JpaRepository<HomePageConfiguration, Long> {

    Optional<HomePageConfiguration> findByApplicationName(String applicationName);
}
