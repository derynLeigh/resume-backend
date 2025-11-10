package com.deryncullen.resume.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    // Track last token generation time to ensure uniqueness
    private volatile long lastTokenGenerationTimeSeconds = 0;

    /**
     * Get expiration time in milliseconds
     */
    public Long getExpirationTime() {
        return expiration;
    }

    /**
     * Get refresh token expiration time in milliseconds
     */
    public Long getRefreshExpirationTime() {
        return refreshExpiration;
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generate token for user
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, expiration);
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, refreshExpiration);
    }

    /**
     * Generate token with custom claims
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return createToken(extraClaims, username, expiration);
    }

    /**
     * Create token with claims, subject, and expiration
     * Ensures each token has a unique identifier even if generated in the same second
     */
    private synchronized String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeSeconds = currentTimeMillis / 1000;

        // JWT spec uses seconds, not milliseconds
        // If we're in the same second as last token, wait 1 second
        if (currentTimeSeconds <= lastTokenGenerationTimeSeconds) {
            try {
                // Sleep for remaining time in current second + 1ms
                long sleepTime = 1000 - (currentTimeMillis % 1000) + 1;
                Thread.sleep(sleepTime);
                currentTimeMillis = System.currentTimeMillis();
                currentTimeSeconds = currentTimeMillis / 1000;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // If interrupted, just use current time
                currentTimeMillis = System.currentTimeMillis();
                currentTimeSeconds = currentTimeMillis / 1000;
            }
        }

        lastTokenGenerationTimeSeconds = currentTimeSeconds;

        // Add a unique identifier to ensure tokens are different even with same iat
        claims.put("jti", UUID.randomUUID().toString());

        log.debug("Generating token for {} with iat: {} ({}s)", subject, currentTimeMillis, currentTimeSeconds);

        return Jwts
                .builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(currentTimeMillis))
                .expiration(new Date(currentTimeMillis + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername.equals(username) && !isTokenExpired(token);
            log.debug("Token validation for {}: {}", username, isValid);
            return isValid;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}