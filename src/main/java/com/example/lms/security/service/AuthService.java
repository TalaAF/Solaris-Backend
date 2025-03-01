package com.example.lms.security.service;

import com.example.lms.security.dto.AuthResponse;
import com.example.lms.security.dto.LoginRequest;
import com.example.lms.security.dto.RegisterRequest;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling authentication operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authenticates a user and generates a JWT token
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.debug("Processing login for user: {}", loginRequest.getEmail());
        
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate JWT token
        log.debug("Generating JWT token for authenticated user: {}", loginRequest.getEmail());
        String jwt = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        
        log.debug("Login successful, returning auth response");
        return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getRole().name());
    }

    /**
     * Registers a new user and authenticates them
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.debug("Processing registration for user: {}", registerRequest.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Email already taken: {}", registerRequest.getEmail());
            throw new IllegalArgumentException("Email is already taken!");
        }

        // Create new user
        log.debug("Creating new user with role: {}", registerRequest.getRole());
        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .profilePicture(registerRequest.getProfilePicture())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.debug("User saved successfully with ID: {}", savedUser.getId());

        // Authenticate the user
        log.debug("Authenticating newly registered user");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate JWT token
        log.debug("Generating JWT token for new user");
        String jwt = tokenProvider.generateToken(authentication);
        
        log.debug("Registration successful, returning auth response");
        return new AuthResponse(jwt, savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
    }
}