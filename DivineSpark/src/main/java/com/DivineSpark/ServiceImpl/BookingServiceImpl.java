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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2: Check if already booked
        if (bookingRepository.existsBySessionAndUserId(session, user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already booked this session!");
        }

        // 3: Check seat availability
        long bookedCount = bookingRepository.countBySession(session);
        if (bookedCount >= session.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is fully booked");
        }

        // 4: Create unique join Token
        String joinToken = UUID.randomUUID().toString();

        // 5: Save Booking
        SessionBooking sessionBooking = new SessionBooking();
        sessionBooking.setSession(session);
        sessionBooking.setUser(user);
        sessionBooking.setPaymentStatus("Free");
        sessionBooking.setJoinToken(joinToken);
        sessionBooking.setBookingDate(LocalDateTime.now());
        bookingRepository.save(sessionBooking);

        // 6: Send email
        String joinLink = "https://yourdomain.com/api/book/join-session?token=" + joinToken;

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
        return "https://paymentgateway.com/pay?sessionId=" + sessionId + "&userId=" + userId;
    }

    @Override
    @Transactional
    public boolean confirmPaidSessionBooking(String paymentId, Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if already booked
        if (bookingRepository.existsBySessionAndUserId(session, user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already booked this session!");
        }

        // Check seat availability
        long bookedCount = bookingRepository.countBySession(session);
        if (bookedCount >= session.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is fully booked");
        }

        String joinToken = UUID.randomUUID().toString();

        SessionBooking sessionBooking = new SessionBooking();
        sessionBooking.setSession(session);
        sessionBooking.setUser(user);
        sessionBooking.setPaymentStatus("PAID");
        sessionBooking.setJoinToken(joinToken);
        sessionBooking.setEmailSentStatus("Sent");
        sessionBooking.setBookingDate(LocalDateTime.now());
        bookingRepository.save(sessionBooking);

        String joinLink = "https://yourdomain.com/api/book/join-session?token=" + joinToken;
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
