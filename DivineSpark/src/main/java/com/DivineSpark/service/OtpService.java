package com.DivineSpark.service;

import com.DivineSpark.model.OtpToken;
import com.DivineSpark.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;


    //generates OTP
    public String generateOtp(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    //Store OTP to DB
    public void saveOtpForEmail(String email,String otp){
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        otpTokenRepository.save(otpToken);
    }

    //Verify
    public boolean verifyOtp(String email,String otp){
        return otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .filter(token -> !token.isVerified())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(token -> token.getOtp().equals(otp))
                .map(token -> {
                    token.setVerified(true);
                    otpTokenRepository.save(token);
                    return true;
                }).orElse(false);
    }
}
