package com.DivineSpark.ServiceImpl;

import com.DivineSpark.model.Session;
import com.DivineSpark.model.SessionBooking;
import com.DivineSpark.model.User;
import com.DivineSpark.repository.BookingRepository;
import com.DivineSpark.repository.SessionRepository;
import com.DivineSpark.repository.UserRepository;
import com.DivineSpark.service.BookingService;
import com.DivineSpark.service.EmailService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    @Override
    public SessionBooking bookFreeSession(Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 1: Check if already booked
        if (bookingRepository.existsBySessionAndUserId(session, user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already booked this session!");
        }

        // 2: Check seat availability
        long bookedCount = bookingRepository.countBySession(session);
        if (bookedCount >= session.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is fully booked");
        }

        // 3: Create unique join token
        String joinToken = UUID.randomUUID().toString();

        // 4: Save Booking
        SessionBooking booking = new SessionBooking();
        booking.setSession(session);
        booking.setUser(user);
        booking.setPaymentStatus("FREE");
        booking.setJoinToken(joinToken);
        booking.setBookingDate(LocalDateTime.now());
        booking.setEmailSentStatus("Sent");
        bookingRepository.save(booking);


        // 5: Send confirmation email
        String joinLink = "https://yourdomain.com/api/book/join-session?token=" + joinToken;
        emailService.sendBookingEmail(
                user.getEmail(),
                "Session Booking Confirmation",
                "You have booked: " + session.getTitle() +
                        "\nJoin link: " + joinLink
        );

        return booking;
    }

    @Override
    public Map<String, Object> initiatePaidSessionBooking(Long sessionId, Long userId) {
        try {
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            int amountInPaise = session.getPrice() * 100; // Razorpay works in paise

            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "receipt_" + sessionId + "_" + userId);

            Order order = razorpayClient.orders.create(options);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayKeyId);

            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment initiation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean confirmPaidSessionBooking(String paymentId, Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // TODO: verify payment signature from Razorpay webhook or frontend response before confirming

        if (bookingRepository.existsBySessionAndUserId(session, user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already booked this session!");
        }

        if (bookingRepository.countBySession(session) >= session.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is fully booked");
        }

        String joinToken = UUID.randomUUID().toString();

        SessionBooking booking = new SessionBooking();
        booking.setSession(session);
        booking.setUser(user);
        booking.setPaymentStatus("PAID");
        booking.setJoinToken(joinToken);
        booking.setEmailSentStatus("Sent");
        booking.setBookingDate(LocalDateTime.now());
        bookingRepository.save(booking);

        String joinLink = "https://yourdomain.com/api/book/join-session?token=" + joinToken;
        emailService.sendBookingEmail(
                user.getEmail(),
                "Session Booking Confirmation",
                "Payment ID: " + paymentId +
                        "\nYou have booked: " + session.getTitle() +
                        "\nJoin link: " + joinLink
        );

        return true;
    }

}
