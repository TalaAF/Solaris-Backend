package com.example.lms.user.repository;

import com.example.lms.user.model.UserProfile;
import com.example.lms.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByUser(User user);
    
    Optional<UserProfile> findByUserId(Long userId);
    
    boolean existsByUser(User user);
}