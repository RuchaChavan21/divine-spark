package com.DivineSpark.dto;

// Used in /auth/register


import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class RegisterUserDTO {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    // The 'roles' field is no longer needed here
}

