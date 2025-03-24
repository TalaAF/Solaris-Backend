package com.example.lms.security.util;

import com.example.lms.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for security operations
 * Provides methods for checking current user's authentication, roles, and permissions
 */
@Component
public class SecurityUtils {

    /**
     * Get the current authenticated user's email
     *
     * @return Optional containing the user's email, or empty if not authenticated
     */
    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return Optional.of(((UserDetails) principal).getUsername());
        } else if (principal instanceof String) {
            return Optional.of((String) principal);
        }
        
        return Optional.empty();
    }
    
    /**
     * Check if the current user has a specific role
     *
     * @param role The role to check (without the "ROLE_" prefix)
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String roleWithPrefix = "ROLE_" + role.toUpperCase();
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }
    
    /**
     * Check if the current user has a specific permission
     *
     * @param permission The permission to check
     * @return true if the user has the permission, false otherwise
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }
    
    /**
     * Check if the current user is an admin
     *
     * @return true if the user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if the current user is an instructor
     *
     * @return true if the user is an instructor, false otherwise
     */
    public static boolean isInstructor() {
        return hasRole("INSTRUCTOR");
    }
    
    /**
     * Check if the current user is a student
     *
     * @return true if the user is a student, false otherwise
     */
    public static boolean isStudent() {
        return hasRole("STUDENT");
    }
    
    /**
     * Check if the current user is the owner of a resource
     *
     * @param user The user to check against
     * @return true if the current user is the owner, false otherwise
     */
    public static boolean isOwner(User user) {
        Optional<String> currentUserEmail = getCurrentUserEmail();
        return currentUserEmail.isPresent() && currentUserEmail.get().equals(user.getEmail());
    }
    
    /**
     * Get all roles and permissions for the current user
     *
     * @return Collection of authority strings
     */
    public static Collection<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Collections.emptyList();
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}