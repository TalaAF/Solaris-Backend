package com.example.lms.security.token.model;

import com.example.lms.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import com.example.lms.common.BaseEntity;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_tokens")
public class UserToken extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String tokenId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    @Column(nullable = false)
    private boolean revoked = false;
    
    // Track device/browser information
    private String userAgent;
    private String ipAddress;
    
    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiryDate);
    }
}
