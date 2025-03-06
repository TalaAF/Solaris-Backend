package com.example.lms.security;

import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom User Details Service
 * 
 * This service implements Spring Security's UserDetailsService interface to 
 * authenticate users based on our custom User entity.
 * 
 * Key responsibilities:
 * 1. Load user from database during authentication
 * 2. Convert our User entity to Spring Security's UserDetails
 * 3. Set up authorities (roles) for the user
 * 4. Handle account status (active/inactive)
 * 
 * This service bridges our application's user model with Spring Security's
 * authentication system.
 */
@Service   // Spring annotation to register as a service
@RequiredArgsConstructor  // Lombok: Creates constructor for final fields
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;  // Injected repository for database access

    /**
     * Loads a user by username (email in our case)
     * This method is called by Spring Security during authentication
     * 
     * @param username The email address of the user trying to authenticate
     * @return UserDetails object used by Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)  // Use read-only transaction for efficiency
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in database by email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

                   if (!user.isActive()) {
        throw new LockedException("User account is locked");
    }

     // Collect authorities from all roles and permissions
     Set<GrantedAuthority> authorities = new HashSet<>();
    
     for (Role role : user.getRoles()) {
         // Add role as an authority with ROLE_ prefix
         authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
         
         // Add each permission as an authority
         for (Permission permission : role.getPermissions()) {
             authorities.add(new SimpleGrantedAuthority(permission.getName()));
         }
     }
        // Convert our User entity to Spring Security's UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())  // Already encoded password
                // Convert our role to Spring Security role with "ROLE_" prefix
                // This prefix is required for @PreAuthorize annotations to work with hasRole()
                .authorities(authorities)
                // Set account status based on isActive flag
                .accountExpired(!user.isActive())
                .accountLocked(!user.isActive())
                .credentialsExpired(!user.isActive())
                .disabled(!user.isActive())
                .build();
    }
}