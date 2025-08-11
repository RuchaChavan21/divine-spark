package com.DivineSpark.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth

                        // Public authentication APIs
                        .requestMatchers("/api/auth/request-otp",
                                "/api/auth/verify-otp",
                                "/api/auth/register",
                                "/api/auth/login").permitAll()

                        // Public GET session endpoints
                        .requestMatchers(HttpMethod.GET,
                                "/api/sessions",
                                "/api/sessions/",
                                "/api/sessions/{id}",
                                "/api/sessions/active/**",
                                "/api/sessions/upcoming",
                                "/api/sessions/search").permitAll()

                        // Admin-only session management
                        .requestMatchers("/api/sessions/admin/**").hasRole("ADMIN")

                        // Bookings require login
                        .requestMatchers("/api/bookings/**").hasAnyRole("USER", "ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
