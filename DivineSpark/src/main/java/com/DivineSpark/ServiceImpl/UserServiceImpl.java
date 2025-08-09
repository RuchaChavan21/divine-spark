package com.DivineSpark.ServiceImpl;


import com.DivineSpark.dto.RegisterUserDTO;
import com.DivineSpark.model.Role;
import com.DivineSpark.model.User;
import com.DivineSpark.repository.OtpTokenRepository;
import com.DivineSpark.repository.RoleRepository;
import com.DivineSpark.repository.UserRepository;
import com.DivineSpark.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public User registerUser(RegisterUserDTO dto) {
        //Check Otp verification
        boolean isVerified = otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(dto.getEmail())
                .map(token -> token.isVerified())
                .orElse(false);

        if(!isVerified) {
            throw new RuntimeException("Email not verified. Please verify first");
        }

        //Create Role
        Set<Role> userRoles = new HashSet<>();
        for(String roleName : dto.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(()-> new RuntimeException("Role not found: "+ roleName));
            userRoles.add(role);
        }

        //Save user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(userRoles);
        user.setVerified(true);

        return userRepository.save(user);


    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

}
