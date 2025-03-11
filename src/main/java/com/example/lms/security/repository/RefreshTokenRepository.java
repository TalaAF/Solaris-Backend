package com.example.lms.security.repository;

import com.example.lms.security.model.RefreshToken;
import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUserAndRevokedFalse(User user);

     /**
     * Find tokens for a user by revocation status
     */
    List<RefreshToken> findByUserAndRevoked(User user, boolean revoked);
    
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId AND r.revoked = false")
    int revokeAllUserTokens(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.id = :userId")
    int deleteAllUserTokens(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    void deleteAllExpiredTokens(Instant now);

    long countByUserId(Long userId);

    List<RefreshToken> findByUserId(Long userId);
}