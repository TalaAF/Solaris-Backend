package com.example.lms.user.service;

import com.example.lms.user.dto.UserCreateRequest;
import com.example.lms.user.dto.UserDTO;
import com.example.lms.user.dto.UserUpdateRequest;
import com.example.lms.user.mapper.UserMapper;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id)));
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        return userMapper.toDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email)));
    }
    
    @Transactional
    public UserDTO createUser(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = userMapper.toEntity(userCreateRequest);
        return userMapper.toDto(userRepository.save(user));
    }
    
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateRequest userUpdateRequest, UserDetails currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        // Check if the current user has permission to update this user
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSameUser = user.getEmail().equals(currentUser.getUsername());
        
        if (!isAdmin && !isSameUser) {
            throw new AccessDeniedException("You don't have permission to update this user");
        }
        
        // Update only non-null fields
        if (userUpdateRequest.getEmail() != null && !user.getEmail().equals(userUpdateRequest.getEmail())) {
            if (userRepository.existsByEmail(userUpdateRequest.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(userUpdateRequest.getEmail());
        }
        
        userMapper.updateEntity(user, userUpdateRequest);
        
        return userMapper.toDto(userRepository.save(user));
    }
    
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }
    
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setActive(true);
        userRepository.save(user);
    }
}