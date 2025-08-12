package com.DivineSpark.controller;

import com.DivineSpark.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<String> requestReset(@RequestBody EmailRequest request) {
        passwordResetService.createPasswordResetToken(request.getEmail());
        return ResponseEntity.ok("Password reset token sent to your email.");
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(passwordResetService.validatePasswordResetToken(token));
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password successfully reset.");
    }
}

class EmailRequest {
    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

class ResetPasswordRequest {
    private String token;
    private String newPassword;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
