package com.DivineSpark.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    // IMPORTANT: This now expects a raw string secret, NOT Base64 encoded.
    // Make sure it is at least 32 characters long.
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs}")
    private long jwtExpirationMs;

    /**
     * Creates a signing key from the raw string secret.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT for a user with the given email and roles.
     */
    public String generateToken(String email, Set<?> roles) {
        Set<String> roleNames = roles.stream()
                .map(Object::toString) // Assumes Role.toString() returns the role name
                .collect(Collectors.toSet());

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roleNames)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * A central method to parse the claims from a token.
     * This is more efficient than parsing the token multiple times.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the email (subject) from the token.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the roles from the token's claims.
     * This version is more robust and handles the case where roles might be a list.
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Claims claims = parseClaims(token);
        Object rolesObject = claims.get("roles");
        if (rolesObject instanceof java.util.List<?>) {
            return ((java.util.List<?>) rolesObject).stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }
        return Set.of(); // Return an empty set if roles are not found or not in the expected format
    }

    /**
     * A generic method to extract a specific claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Checks if the token has expired.
     */
    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validates the token against the user's email.
     */
    public boolean validateToken(String token, String email) {
        final String tokenUsername = extractEmail(token);
        return (tokenUsername.equals(email) && !isTokenExpired(token));
    }
}
