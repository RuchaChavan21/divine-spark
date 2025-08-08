package com.DivineSpark.repository;

import com.DivineSpark.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //for login and checking user by email
    Optional<User> findByEmail(String email);

    //for registration if email already exist
    boolean existsByEmail(String email);
}
