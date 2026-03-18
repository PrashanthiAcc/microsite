package com.ix.manufacturinglab.repository;

import com.ix.manufacturinglab.entity.UserManagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserManagement, Integer> {

    List<UserManagement> findByIsActiveTrue();
}