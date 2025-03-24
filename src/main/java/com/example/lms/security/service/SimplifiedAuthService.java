package com.example.lms.security.service;

import com.example.lms.security.dto.AuthResponse;
import com.example.lms.security.dto.LoginRequest;
import com.example.lms.security.dto.LogoutRequest;
import com.example.lms.security.dto.RegisterRequest;
import com.example.lms.security.exception.TokenRefreshException;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.security.model.RefreshToken;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Processing login request for user: {}", loginRequest.getEmail());

        // Authenticate user with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT access token
        String jwt = tokenProvider.generateToken(authentication);

        // Get user from repository
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // Join role names with comma for the response
        String roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        log.info("User {} successfully logged in", user.getEmail());

        return new AuthResponse(jwt, refreshToken.getToken(), user.getId(), user.getEmail(), roleNames);
    }

    /**
     * Register a new user and automatically log them in
     *
     * @param registerRequest User registration data
     * @return AuthResponse with tokens and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Processing registration for user: {}", registerRequest.getEmail());

        // Check if email is already taken
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
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

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());

        // Authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()));

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT access token
        String jwt = tokenProvider.generateToken(authentication);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        // Join role names with comma for the response
        String roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        log.info("User {} successfully registered", savedUser.getEmail());

        // Return AuthResponse with tokens and user info
        return new AuthResponse(jwt, refreshToken.getToken(), savedUser.getId(), savedUser.getEmail(), roleNames);
    }

    /**
     * Create an authentication token for a user
     * Used when refreshing tokens
     */
    public Authentication createAuthenticationToken(User user) {
        // Create authorities from roles
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role-based authorities (ROLE_XXX)
        authorities.addAll(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet()));

        // Add permission-based authorities
        user.getRoles().forEach(role -> {
            role.getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            });
        });
        
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(!user.isActive())
                .accountLocked(!user.isActive())
                .credentialsExpired(!user.isActive())
                .disabled(!user.isActive())
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Transactional
public boolean logout(LogoutRequest logoutRequest) {
    log.info("Processing logout request");
    
    try {
        // Get token from request
        String refreshToken = logoutRequest.getRefreshToken();
        
        // Find the token
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
            .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token not found"));
            
        // Get user
        User user = token.getUser();
        
        // Increment token version to invalidate all existing tokens
        user.incrementTokenVersion();
        userRepository.save(user);
        
        // Revoke all refresh tokens
        refreshTokenService.revokeAllUserTokens(user.getId());
        
        log.info("User {} successfully logged out", user.getEmail());
        return true;
    } catch (Exception e) {
        log.error("Error during logout: {}", e.getMessage());
        return false;
    }
}
}