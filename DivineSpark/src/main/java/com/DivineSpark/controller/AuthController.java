package com.DivineSpark.controller;

import com.DivineSpark.dto.EmailRequestDTO;
import com.DivineSpark.dto.LoginRequestDTO;
import com.DivineSpark.dto.OTPVerificationDTO;
import com.DivineSpark.dto.RegisterUserDTO;
import com.DivineSpark.model.User;
import com.DivineSpark.service.EmailService;
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
    private final EmailService emailService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;



    // 1: Request OTP
    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody EmailRequestDTO request) {
        String otp = otpService.generateOtp();
        otpService.saveOtpForEmail(request.getEmail(), otp);
        emailService.sendEmail(request.getEmail(), otp);
        return ResponseEntity.ok("OTP sent to " + request.getEmail());
    }

    // 2: Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OTPVerificationDTO request) {
        boolean verified = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return verified
                ? ResponseEntity.ok("OTP verified successfully.")
                : ResponseEntity.badRequest().body("Invalid or expired OTP.");
    }

    // 3: Register user
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterUserDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("User registered successfully!");
    }

    // 4: Login

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        // 1. Attempt to authenticate the user using the provided credentials.
        // The AuthenticationManager will use your CustomUserDetailsService and PasswordEncoder.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // If credentials are bad, return a 401 Unauthorized response.
            return ResponseEntity.status(401).body("Error: Invalid username or password");
        }

        // 2. If authentication is successful, load the user details again.
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        // 3. Extract the roles (authorities) from the UserDetails object.
        // We convert the Collection<GrantedAuthority> to a Set<String>.
        final Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 4. Generate the JWT using the email and the extracted set of roles.
        // This now matches the signature of your generateToken method.
        final String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

        // 5. Create a response object and return the token to the client.
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }



}
