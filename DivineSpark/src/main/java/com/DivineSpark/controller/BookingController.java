package com.DivineSpark.controller;

import com.DivineSpark.model.SessionBooking;
import com.DivineSpark.model.User;
import com.DivineSpark.repository.BookingRepository;
import com.DivineSpark.repository.UserRepository;
import com.DivineSpark.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;


    // Free booking
    @PostMapping("/free/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionBooking> bookFreeSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String email = (String) authentication.getPrincipal();
        long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        log.info("Free booking request: sessionId={}, userId={}", sessionId, userId);

        return ResponseEntity.ok(bookingService.bookFreeSession(sessionId, userId));
    }

    @PostMapping("/paid/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> initiatePaidSessionBooking(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String email = (String) authentication.getPrincipal();
        long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        log.info("Paid booking initiation: sessionId={}, userId={}", sessionId, userId);

        Map<String, Object> paymentData = bookingService.initiatePaidSessionBooking(sessionId, userId);
        return ResponseEntity.ok(paymentData);
    }

    // Paid booking - Step 2 (confirm after payment success)
    @PostMapping("/paid/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> confirmPaidSessionBooking(
            @RequestParam String paymentId,
            @RequestParam Long sessionId,
            Authentication authentication) {

        String email = (String) authentication.getPrincipal();
        long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        log.info("Payment confirmation: paymentId={}, sessionId={}, userId={}", paymentId, sessionId, userId);

        boolean success = bookingService.confirmPaidSessionBooking(paymentId, sessionId, userId);
        return ResponseEntity.ok(success ? "Booking successful" : "Booking failed");
    }

    // Join session using token
    @GetMapping("/join-session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> joinSession(
            @RequestParam String token,
            Authentication authentication) {

        String email = (String) authentication.getPrincipal();
        log.info("Join link accessed with token: {}", token);

        Optional<SessionBooking> bookingOpt = bookingRepository.findByJoinToken(token);
        if (bookingOpt.isEmpty()) {
            log.warn("No booking found for token: {}", token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired join link");
        }

        SessionBooking booking = bookingOpt.get();
        if (!booking.getUser().getEmail().equals(email)) {
            log.warn("User email mismatch for join link: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This link is not for you");
        }

        String zoomLink = booking.getSession().getZoomLink();
        if (zoomLink == null || zoomLink.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Zoom link not available.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(zoomLink));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
