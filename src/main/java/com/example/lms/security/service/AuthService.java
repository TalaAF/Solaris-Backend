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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
        log.info("Attempting login for: {}", loginRequest.getEmail());
        
        try {
            // First check if user exists and is active
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            
            if (!user.isActive()) {
                log.warn("Login attempt for inactive account: {}", loginRequest.getEmail());
                throw new DisabledException("User account is locked");
            }
            
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            log.info("Login successful for user: {}", loginRequest.getEmail());
            return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getRole().name());
            
        } catch (DisabledException e) {
            log.warn("Login attempt for disabled account: {}", loginRequest.getEmail());
            throw new DisabledException("User account is locked");
        } catch (LockedException e) {
            log.warn("Login attempt for locked account: {}", loginRequest.getEmail());
            throw new LockedException("User account is locked");
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        } catch (AuthenticationException e) {
            log.error("Authentication error: {}", e.getMessage());
            throw new BadCredentialsException("Authentication failed");
        }
    }

    // Register method unchanged
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Attempting registration for: {}", registerRequest.getEmail());
        
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken!");
        }

        // Create new user with encoded password
        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .profilePicture(registerRequest.getProfilePicture())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());

        // Authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return new AuthResponse(jwt, savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
    }
}