package com.DivineSpark.dto;

//used in /auth/verify-otp
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPVerificationDTO {

    private String email;
    private String otp;
}
