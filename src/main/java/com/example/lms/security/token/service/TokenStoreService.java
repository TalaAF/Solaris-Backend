package com.example.lms.security.token.service;

import com.example.lms.security.token.model.UserToken;
import com.example.lms.security.token.repository.UserTokenRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenStoreService {
    
    private final UserTokenRepository tokenRepository;
    private final UserRepository userRepository;


    
    // Create a new token
    public UserToken createToken(User user, String userAgent, String ipAddress) {
        UserToken token = new UserToken();
        token.setTokenId(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS)); // 7 days validity
        token.setUserAgent(userAgent);
        token.setIpAddress(ipAddress);
        
        return tokenRepository.save(token);
    }
    
    // Validate a token
    // Validate a token
public boolean validateToken(String tokenId) {
    if (tokenId == null || tokenId.isEmpty()) {
        return false;
    }
    
    Optional<UserToken> tokenOpt = tokenRepository.findByTokenId(tokenId);
    if (tokenOpt.isEmpty()) {
        return false;
    }
    
    UserToken token = tokenOpt.get();
    
    // Check if token is not revoked and not expired
    boolean isValid = !token.isRevoked() && Instant.now().isBefore(token.getExpiryDate());
    
    if (!isValid) {
        if (token.isRevoked()) {
            log.debug("Token {} was revoked", tokenId);
        } else {
            log.debug("Token {} expired at {}", tokenId, token.getExpiryDate());
        }
    }
    
    return isValid;
}
    
    // Revoke a specific token
    public void revokeToken(String tokenId) {
        tokenRepository.findByTokenId(tokenId).ifPresent(token -> {
            token.setRevoked(true);
            tokenRepository.save(token);
        });
    }
    
    // Revoke all tokens for a user
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<UserToken> tokens = tokenRepository.findByUser(user);
        tokens.forEach(token -> token.setRevoked(true));
        tokenRepository.saveAll(tokens);
    }
    
    // Clean up expired tokens
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM daily
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}