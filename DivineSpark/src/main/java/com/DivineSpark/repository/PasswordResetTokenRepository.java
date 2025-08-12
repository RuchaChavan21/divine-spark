package com.DivineSpark.repository;

import com.DivineSpark.model.PasswordResetToken;
import com.DivineSpark.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);
    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);
}
