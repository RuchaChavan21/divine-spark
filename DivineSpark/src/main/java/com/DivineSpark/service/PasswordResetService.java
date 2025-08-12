package com.DivineSpark.service;

import com.DivineSpark.model.User;

public interface PasswordResetService {
    void createPasswordResetToken(String email);
    boolean validatePasswordResetToken(String token);
    void resetPassword(String token, String newPassword);
}
