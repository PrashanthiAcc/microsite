package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UserManagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserManagement, Integer> {

    List<UserManagement> findByIsActiveTrue();

    boolean existsByUserEid(String userEid);
    Optional<UserManagement> findByUserEid(String userEid);

    boolean existsByUserEidAndUserPassword(String userEid, String userPassword);
}