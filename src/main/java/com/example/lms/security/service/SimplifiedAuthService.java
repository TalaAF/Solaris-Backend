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

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
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
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Processing login request for user: {}", loginRequest.getEmail());

        // Authenticate user with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT access token
        String jwt = tokenProvider.generateToken(authentication, request);
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Generate refresh token 
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        // Get user from repository with all relationships

        Claims claims = tokenProvider.parseToken(jwt);
    String tokenId = claims.get("tokenId", String.class);

    if (tokenId == null) {
        throw new IllegalStateException("Token generation failed to include tokenId");
    }

        // Join role names with comma for the response
        String roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        log.info("User {} successfully logged in", user.getEmail());

        // Construct auth response with complete role information
        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setName(user.getFullName());
        userInfo.setProfileImage(user.getProfilePicture());
        userInfo.setRoles(user.getRoles());  // Use the complete roles from the user entity
        
        response.setUser(userInfo);
        
        return response;
    }

    /**
     * Register a new user and automatically log them in
     *
     * @param registerRequest User registration data
     * @return AuthResponse with tokens and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest request) {
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
        String jwt = tokenProvider.generateToken(authentication, request);

        // Get tokenId from generated JWT
        Claims claims = tokenProvider.parseToken(jwt);
        String tokenId = claims.get("tokenId", String.class);
        
        // Reload the user to ensure we have all relationships populated
        User freshUser = userRepository.findById(savedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found after save"));
        
        // Pass the complete set of roles directly
        Set<Role> completeRoles = freshUser.getRoles();
        
        log.info("User {} successfully registered", freshUser.getEmail());

        // Construct auth response with complete role information
        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(freshUser.getId());
        userInfo.setEmail(freshUser.getEmail());
        userInfo.setName(freshUser.getFullName());
        userInfo.setProfileImage(freshUser.getProfilePicture());
        userInfo.setRoles(completeRoles);
        
        response.setUser(userInfo);
        
        return response;
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