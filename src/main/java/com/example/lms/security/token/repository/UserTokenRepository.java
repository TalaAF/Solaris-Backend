package com.example.lms.security.token.repository;

import com.example.lms.security.token.model.UserToken;
import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenId(String tokenId);
    List<UserToken> findByUser(User user);
    void deleteByExpiryDateBefore(Instant date);
    boolean existsByTokenIdAndRevokedFalse(String tokenId);
}