package com.DivineSpark.service;

import com.DivineSpark.dto.RegisterUserDTO;
import com.DivineSpark.model.User;

public interface UserService {

    User registerUser(RegisterUserDTO dto);
    User findByEmail(String email);
}
