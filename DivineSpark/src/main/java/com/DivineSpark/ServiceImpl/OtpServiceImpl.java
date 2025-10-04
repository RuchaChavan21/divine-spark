package com.DivineSpark.ServiceImpl;

import com.DivineSpark.model.OtpToken;
import com.DivineSpark.repository.OtpTokenRepository;
import com.DivineSpark.repository.UserRepository;
import com.DivineSpark.service.EmailService;
import com.DivineSpark.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final long OTP_EXPIRATION_MINUTES = 5;


    //generates OTP
    /*public String generateOtp(){
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }*/

    public String generateAndCacheOtp(String email) {

        //Generate OTP
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(999999));

        //Cache in Redis
        String redisKey = "otp:" + email;
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        log.info("Cached OTP for email: " + email);

        //Trigger the slow API call in background
        sendOtpViaSlowApi(email, otp);

        return otp;
    }

    //Store OTP to DB
    /*public void saveOtpForEmail(String email,String otp){
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        otpTokenRepository.save(otpToken);
    }*/

    //
    @Async
    public void sendOtpViaSlowApi(String email, String otp) {
        log.info("Attempting to send OTP {} to email {}", otp, email);
        try {
            // This simulates your slow API call, you can remove it if your email service is fast
            //Thread.sleep(5000);

            // ✅ Uncomment this line to actually send the email
            emailService.sendEmail(email, "Your DivineSpark OTP is: " + otp);

            log.info("Successfully sent OTP to email {} via slow API.", email);

        } catch (Exception e) {
            // If the email service fails, this will now catch the error
            log.error("Failed to send OTP to email {}. Error: {}", email, e.getMessage());
        }
    }

    //Verify
    /*@Transactional
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
    }*/

    public boolean verifyOtp(String email, String userSubmittedOtp) {
        String redisKey = "otp:" + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if(userSubmittedOtp == null || !userSubmittedOtp.equals(storedOtp)) {
            log.warn("OTP verification failed for email: " + email);
            return false;
        }

        //OTP is correct, now update the DB
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setVerified(true);
            userRepository.save(user);
            log.info("OTP verified successfully for email: " + email);
        });

        redisTemplate.delete(redisKey);
        log.info("OTP verified successfully for email: " + email);

        return true;
    }
}
