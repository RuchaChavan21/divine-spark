package com.DivineSpark.service;

public interface OtpService {
    public String generateAndCacheOtp(String email);
    public void sendOtpViaSlowApi(String email, String otp);
    public boolean verifyOtp(String email, String userSubmittedOtp);
}
