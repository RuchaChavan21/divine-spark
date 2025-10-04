package com.DivineSpark.controller;

import com.DivineSpark.dto.EmailRequestDTO;
import com.DivineSpark.dto.LoginRequestDTO;
import com.DivineSpark.dto.OTPVerificationDTO;
import com.DivineSpark.dto.RegisterUserDTO;
import com.DivineSpark.service.OtpService;
import com.DivineSpark.service.UserService;
import com.DivineSpark.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.DivineSpark.config.CustomUserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpService otpService;
    // 🔽 The EmailService is no longer needed in this controller
    // private final EmailService emailService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * Handles the OTP request.
     * The logic is now fully encapsulated in the OtpService.
     */
    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody EmailRequestDTO request) {
        // ✅ --- REFACTORED ---
        // This single method call now does everything:
        // 1. Generates a new OTP.
        // 2. Caches it in Redis with an expiry.
        // 3. Triggers the slow email-sending API in a background thread.
        otpService.generateAndCacheOtp(request.getEmail());

        // We can immediately return a success response to the user.
        return ResponseEntity.ok("OTP has been sent to " + request.getEmail());
    }

    /**
     * Handles OTP verification.
     * No changes are needed here, as the service method's signature is the same.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OTPVerificationDTO request) {
        boolean verified = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return verified
                ? ResponseEntity.ok("OTP verified successfully.")
                : ResponseEntity.badRequest().body("Invalid or expired OTP.");
    }

    // 3: Register user (No changes needed)
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterUserDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("User registered successfully!");
    }

    // 4: Login (No changes needed)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Error: Invalid username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        final Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        final String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}