package com.example.lms.security.service;

import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {
    
    private final UserRepository userRepository;
    
    /**
     * Check if the current authenticated user is the same as the user with the given ID
     */
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return userRepository.findById(userId)
            .map(user -> user.getEmail().equals(authentication.getName()))
            .orElse(false);
    }
}