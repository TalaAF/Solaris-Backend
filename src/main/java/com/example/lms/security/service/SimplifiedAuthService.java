package com.example.lms.security.service;

import com.example.lms.security.dto.AuthResponse;
import com.example.lms.security.dto.LoginRequest;
import com.example.lms.security.dto.RegisterRequest;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.security.model.Role;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplifiedAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RoleRepository roleRepository;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for: {}", loginRequest.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        
        // Join role names with comma for the response
        String roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));
        
        return new AuthResponse(jwt, user.getId(), user.getEmail(), roleNames);
    }

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
                .profilePicture(registerRequest.getProfilePicture())
                .isActive(true)
                .build();
                
        // Assign roles
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoleNames() != null && !registerRequest.getRoleNames().isEmpty()) {
            for (String roleName : registerRequest.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            // Default to STUDENT role if none specified
            Role defaultRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new EntityNotFoundException("Default role not found"));
            roles.add(defaultRole);
        }
        user.setRoles(roles);

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
        
        // Join role names with comma for the response
        String roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));
        
        return new AuthResponse(jwt, savedUser.getId(), savedUser.getEmail(), roleNames);
    }
}