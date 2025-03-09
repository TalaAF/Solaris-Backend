package com.example.lms.security.model;

import com.example.lms.common.BaseEntity;
import com.example.lms.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    @Column(nullable = false)
    private boolean used = false;
    
    /**
     * Factory method to create a new token for a user
     * @param user The user for whom to create the token
     * @param expirationMinutes How long the token is valid (in minutes)
     * @return New PasswordResetToken
     */
    public static PasswordResetToken createTokenForUser(User user, int expirationMinutes) {
        return PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(expirationMinutes * 60))
                .used(false)
                .build();
    }
    
    /**
     * Check if the token is expired
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
    
    /**
     * Check if the token is valid (not expired and not used)
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }
}