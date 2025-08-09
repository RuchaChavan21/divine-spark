package com.DivineSpark.service;

public interface OtpService {
    public String generateOtp();
    public void saveOtpForEmail(String email,String otp);
    public boolean verifyOtp(String email,String otp);
}
