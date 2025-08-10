package com.DivineSpark.ServiceImpl;

import com.DivineSpark.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();;
        message.setTo(toEmail);
        message.setSubject("Your Divine Spark OTP");
        message.setText("Your OTP for Divine Spark registration is " + otp + "\nThis OTP will expire in 5 minutes");
        mailSender.send(message);
    }
}
