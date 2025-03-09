package com.example.lms.security.service;

import com.example.lms.security.exception.TokenRefreshException;
import com.example.lms.security.model.RefreshToken;
import com.example.lms.security.repository.RefreshTokenRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    @Value("${app.jwt.refresh-token.expiration-ms:604800000}") // Default 7 days
    private long refreshTokenExpirationMs;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a new refresh token for a user
     * If a valid refresh token already exists, return it
     * Otherwise create a new one
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        refreshTokenRepository.deleteAllUserTokens(userId);

        // Check if user already has a valid refresh token
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserAndRevokedFalse(user);
        if (existingToken.isPresent() && existingToken.get().isValid()) {
            return existingToken.get();
        }
        
        // Revoke any existing tokens
        refreshTokenRepository.revokeAllUserTokens(userId);
        
        // Create new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        
        log.info("Created new refresh token for user: {}", user.getEmail());
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * Verify if a refresh token is valid
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new login");
        }
        
        if (token.isRevoked()) {
            throw new TokenRefreshException(token.getToken(), "Refresh token was revoked. Please make a new login");
        }
        
        return token;
    }
    
    /**
     * Find a refresh token by token string
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * Revoke a specific refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token not found"));
        
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
    }
    
    /**
     * Revoke all tokens for a specific user
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
        log.info("Revoked all refresh tokens for user ID: {}", userId);
    }
    
    /**
     * Scheduled task to clean up expired tokens
     * Runs once a day at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
        log.info("Cleaned up expired refresh tokens");
    }
}