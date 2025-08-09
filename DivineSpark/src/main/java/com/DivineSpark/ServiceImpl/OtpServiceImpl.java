package com.DivineSpark.ServiceImpl;

import com.DivineSpark.model.OtpToken;
import com.DivineSpark.repository.OtpTokenRepository;
import com.DivineSpark.repository.UserRepository;
import com.DivineSpark.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;


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
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .filter(token -> !token.isVerified())
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(token -> token.getOtp().equals(otp))
                .map(token -> {
                    token.setVerified(true);
                    otpTokenRepository.save(token);
                    log.info("OTP verified and saved for email: {}", email);

                    userRepository.findByEmail(email).ifPresent(user -> {
                        user.setVerified(true);
                        userRepository.save(user);
                        log.info("User verified flag updated for email: {}", email);
                    });

                    return true;
                }).orElseGet(() -> {
                    log.warn("OTP verification failed for email: {}", email);
                    return false;
                });
    }
}
