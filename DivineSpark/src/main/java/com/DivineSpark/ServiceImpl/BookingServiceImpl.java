package com.DivineSpark.ServiceImpl;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionBooking;
import com.DivineSpark.model.User;
import com.DivineSpark.repository.BookingRepository;
import com.DivineSpark.repository.SessionRepository;
import com.DivineSpark.repository.UserRepository;
import com.DivineSpark.service.BookingService;
import com.DivineSpark.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Override
    public SessionBooking bookFreeSession(Long sessionId, Long userId) {
        // 1: Fetch Session and user
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2: Check seat availability
        long bookedCount = bookingRepository.countBySession(session);
        if (bookedCount >= session.getCapacity()) {
            throw new RuntimeException("Session is fully booked");
        }

        // 3: Create unique join Token
        String joinToken = UUID.randomUUID().toString();

        // 4: Save Booking
        SessionBooking sessionBooking = new SessionBooking();
        sessionBooking.setSession(session);
        sessionBooking.setUser(user);
        sessionBooking.setPaymentStatus("Free");
        sessionBooking.setJoinToken(joinToken);
        sessionBooking.setBookingDate(LocalDateTime.now());
        bookingRepository.save(sessionBooking);

        // 5: Send email
        String joinLink = "https://yourdomain.com/api/book/join-session?token=" + joinToken;

        /*
         TO (future version for your real frontend):
         String joinLink = "https://www.your-actual-website.com/join?token=" + joinToken;

         Example for a React app running on localhost:3000:
         String joinLink = "http://localhost:3000/join?token=" + joinToken;
        */

        emailService.sendBookingEmail(
                user.getEmail(),
                "Your Session Booking Confirmation",
                "You have booked: " + session.getTitle() +
                        "\nJoin link: " + joinLink
        );

        return sessionBooking;
    }

    @Override
    public String initiatePaidSessionBooking(Long sessionId, Long userId) {
        // Payment gateway logic (Stripe/Razorpay) — create payment order
        // For now, we return a dummy payment URL
        return "https://paymentgateway.com/pay?sessionId=" + sessionId + "&userId=" + userId;
    }

    @Override
    @Transactional
    public boolean confirmPaidSessionBooking(String paymentId, Long sessionId, Long userId) {
        // 1: Fetch Session and user
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2: Check seat availability
        long bookedCount = bookingRepository.countBySession(session);
        if (bookedCount >= session.getCapacity()) {
            throw new RuntimeException("Session is fully booked");
        }

        // 3: Create unique join Token
        String joinToken = UUID.randomUUID().toString();

        // 4: Save Booking
        SessionBooking sessionBooking = new SessionBooking();
        sessionBooking.setSession(session);
        sessionBooking.setUser(user);
        sessionBooking.setPaymentStatus("PAID");
        sessionBooking.setJoinToken(joinToken);
        sessionBooking.setEmailSentStatus("Sent");
        sessionBooking.setBookingDate(LocalDateTime.now());
        bookingRepository.save(sessionBooking);

        // 5: Send email
        String joinLink = "https://yourdomain.com/api/book/join-session?token=" + joinToken;

        /*
         TO (future version for your real frontend):
         String joinLink = "https://www.your-actual-website.com/join?token=" + joinToken;

         Example for a React app running on localhost:3000:
         String joinLink = "http://localhost:3000/join?token=" + joinToken;
        */

        emailService.sendBookingEmail(
                user.getEmail(),
                "Your Session Booking Confirmation",
                "Payment ID: " + paymentId +
                        "\nYou have booked: " + session.getTitle() +
                        "\nJoin link: " + joinLink
        );

        return true;
    }
}
