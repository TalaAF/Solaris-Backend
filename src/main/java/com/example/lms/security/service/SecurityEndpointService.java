package com.example.lms.security.service;

import com.example.lms.security.dto.SecurityEndpointDTO;
import com.example.lms.security.model.Permission;
import com.example.lms.security.model.SecurityEndpoint;
import com.example.lms.security.repository.PermissionRepository;
import com.example.lms.security.repository.SecurityEndpointRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityEndpointService {
    
    private final SecurityEndpointRepository securityEndpointRepository;
    private final PermissionRepository permissionRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Transactional(readOnly = true)
    public boolean checkPermission(Authentication authentication, String httpMethod, String requestPath) {
        // Get all endpoints for this HTTP method
        List<SecurityEndpoint> endpoints = securityEndpointRepository.findByHttpMethod(httpMethod);
        
        // Default to true if no matching patterns (could also default to false)
        boolean requiresPermission = false;
        String requiredPermission = null;
        
        // Find the most specific matching endpoint
        for (SecurityEndpoint endpoint : endpoints) {
            if (pathMatcher.match(endpoint.getPathPattern(), requestPath)) {
                requiresPermission = true;
                requiredPermission = endpoint.getRequiredPermission().getName();
                // Check if the user has the required permission
                if (hasPermission(authentication, requiredPermission)) {
                    return true;
                }
            }
        }
        
        // If we found matching endpoints but user has none of the required permissions
        if (requiresPermission) {
            return false;
        }
        
        // If no specific endpoint matches, allow access by default
        return true;
    }
    
    @Transactional(readOnly = true)
    public boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }
    
    private boolean hasPermission(Authentication authentication, String permission) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(permission));
    }
    
    @Transactional(readOnly = true)
    public List<SecurityEndpointDTO> getAllSecurityEndpoints() {
        return securityEndpointRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public SecurityEndpointDTO createSecurityEndpoint(SecurityEndpointDTO dto) {
        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));
        
        SecurityEndpoint endpoint = new SecurityEndpoint();
        endpoint.setHttpMethod(dto.getHttpMethod());
        endpoint.setPathPattern(dto.getPathPattern());
        endpoint.setRequiredPermission(permission);
        
        SecurityEndpoint savedEndpoint = securityEndpointRepository.save(endpoint);
        return mapToDto(savedEndpoint);
    }
    
    @Transactional
    public SecurityEndpointDTO updateSecurityEndpoint(Long id, SecurityEndpointDTO dto) {
        SecurityEndpoint endpoint = securityEndpointRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Security endpoint not found"));
        
        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));
        
        endpoint.setHttpMethod(dto.getHttpMethod());
        endpoint.setPathPattern(dto.getPathPattern());
        endpoint.setRequiredPermission(permission);
        
        SecurityEndpoint updatedEndpoint = securityEndpointRepository.save(endpoint);
        return mapToDto(updatedEndpoint);
    }
    
    @Transactional
    public void deleteSecurityEndpoint(Long id) {
        SecurityEndpoint endpoint = securityEndpointRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Security endpoint not found"));
        securityEndpointRepository.delete(endpoint);
    }
    
    private SecurityEndpointDTO mapToDto(SecurityEndpoint endpoint) {
        SecurityEndpointDTO dto = new SecurityEndpointDTO();
        dto.setId(endpoint.getId());
        dto.setHttpMethod(endpoint.getHttpMethod());
        dto.setPathPattern(endpoint.getPathPattern());
        dto.setPermissionId(endpoint.getRequiredPermission().getId());
        dto.setPermissionName(endpoint.getRequiredPermission().getName());
        return dto;
    }
}