package com.example.lms.user.service;

import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }
    
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        // Update only non-null fields
        if (userDetails.getEmail() != null) {
            if (!user.getEmail().equals(userDetails.getEmail()) && 
                    userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(userDetails.getEmail());
        }
        
        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }
        
        if (userDetails.getProfilePicture() != null) {
            user.setProfilePicture(userDetails.getProfilePicture());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }
    
    @Transactional
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setActive(true);
        userRepository.save(user);
    }
}