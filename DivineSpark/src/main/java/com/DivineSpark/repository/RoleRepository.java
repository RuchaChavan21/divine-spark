package com.DivineSpark.repository;

import com.DivineSpark.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    //Assign and check role
    Optional<Role> findByName(String name);
}
