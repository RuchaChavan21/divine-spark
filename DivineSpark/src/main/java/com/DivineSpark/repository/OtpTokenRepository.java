package com.DivineSpark.repository;

import com.DivineSpark.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    //finds latest OTP for a given email.
    Optional<OtpToken> findTopByEmailOrderByCreatedAtDesc(String email);
}
