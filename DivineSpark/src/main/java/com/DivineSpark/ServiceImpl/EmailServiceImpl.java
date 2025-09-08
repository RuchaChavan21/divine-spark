package com.DivineSpark.ServiceImpl;

import com.DivineSpark.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendEmail(String toEmail, String otp) {
        System.out.println("📧 Sending OTP email in thread: " + Thread.currentThread().getName());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Divine Spark OTP");
        message.setText("Your OTP for Divine Spark registration is " + otp +
                "\nThis OTP will expire in 5 minutes");

        try {
            Thread.sleep(3000); // simulate delay to see async clearly
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mailSender.send(message);
        System.out.println("✅ Email sent to: " + toEmail);
    }


    @Override
    @Async   // 👈 booking emails also async
    public void sendBookingEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message); // now async
    }
}
