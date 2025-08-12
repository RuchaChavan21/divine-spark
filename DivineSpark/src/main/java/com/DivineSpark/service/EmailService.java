package com.DivineSpark.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

public interface EmailService {
    public void sendEmail(String toEmail, String otp);

    void sendBookingEmail(String to, String subject, String body);

}
