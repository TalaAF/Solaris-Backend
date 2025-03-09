package com.example.lms.security.service;

import com.example.lms.security.dto.ForgotPasswordRequest;
import com.example.lms.security.dto.ResetPasswordRequest;
import com.example.lms.security.model.PasswordResetToken;
import com.example.lms.security.repository.PasswordResetTokenRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {
    
    @Value("${app.password-reset.token-expiration-minutes:30}") // Default 30 minutes
    private int tokenExpirationMinutes;
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    // We'll add email service here later
    
    /**
     * Process a forgot password request and generate a reset token
     */
    @Transactional
    public String processForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + request.getEmail()));
        
        // Invalidate all existing tokens for this user
        tokenRepository.invalidateAllUserTokens(user.getId());
        
        // Create new token
        PasswordResetToken token = PasswordResetToken.createTokenForUser(user, tokenExpirationMinutes);
        tokenRepository.save(token);
        
        log.info("Generated password reset token for user: {}", user.getEmail());
        
        // TODO: Send email with token
        
        return token.getToken(); // Return token for testing purposes - in production we wouldn't return this
    }
    
    /**
     * Reset a user's password using a valid token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));
        
        if (!token.isValid()) {
            if (token.isUsed()) {
                throw new IllegalArgumentException("Token has already been used");
            } else {
                throw new IllegalArgumentException("Token has expired");
            }
        }
        
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        
        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);
        
        log.info("Reset password for user: {}", user.getEmail());
    }
    
    /**
     * Validate if a token is valid (not expired and not used)
     */
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        return resetToken.isPresent() && resetToken.get().isValid();
    }
    
    /**
     * Get all valid tokens for a user
     */
    public List<PasswordResetToken> getValidTokensForUser(User user) {
        return tokenRepository.findByUserAndUsedFalse(user);
    }
    
    /**
     * Scheduled task to clean up expired tokens
     * Runs once a day at midnight
     */
    @Transactional
    // @Scheduled(cron = "0 0 0 * * ?") // Uncomment this once we've properly set up scheduling
    public void cleanupExpiredTokens() {
        tokenRepository.deleteAllExpiredTokens(Instant.now());
        log.info("Cleaned up expired password reset tokens");
    }
    
    /**
     * Check if a password meets the complexity requirements
     */
    public boolean isValidPassword(String password) {
        // At least 8 chars
        if (password.length() < 8) {
            return false;
        }
        
        // Contains at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        
        // Contains at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        
        // Contains at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Contains at least one special character
        if (!password.matches(".*[@#$%^&+=].*")) {
            return false;
        }
        
        // No whitespace
        if (password.matches(".*\\s.*")) {
            return false;
        }
        
        return true;
    }
}