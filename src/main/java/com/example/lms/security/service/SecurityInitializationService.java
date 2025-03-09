package com.example.lms.security.service;

import com.example.lms.security.model.Permission;
import com.example.lms.security.model.Role;
import com.example.lms.security.model.SecurityEndpoint;
import com.example.lms.security.repository.PermissionRepository;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.security.repository.SecurityEndpointRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityInitializationService {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SecurityEndpointRepository securityEndpointRepository;
    private final PasswordEncoder passwordEncoder;
    
    @PostConstruct
    @Transactional
    public void initializeSecurityData() {
        log.info("Initializing security data...");
        
        // Create default permissions
        createPermissionIfNotExists("user:create", "Can create users");
        createPermissionIfNotExists("user:read", "Can view user information");
        createPermissionIfNotExists("user:update", "Can update users");
        createPermissionIfNotExists("user:delete", "Can delete users");
        
        createPermissionIfNotExists("course:create", "Can create courses");
        createPermissionIfNotExists("course:read", "Can view course information");
        createPermissionIfNotExists("course:update", "Can update courses");
        createPermissionIfNotExists("course:delete", "Can delete courses");
        
        createPermissionIfNotExists("department:create", "Can create departments");
        createPermissionIfNotExists("department:read", "Can view department information");
        createPermissionIfNotExists("department:update", "Can update departments");
        createPermissionIfNotExists("department:delete", "Can delete departments");
        
        createPermissionIfNotExists("enrollment:create", "Can enroll in courses");
        createPermissionIfNotExists("enrollment:read", "Can view enrollments");
        createPermissionIfNotExists("enrollment:update", "Can update enrollments");
        createPermissionIfNotExists("enrollment:delete", "Can unenroll from courses");
        
        createPermissionIfNotExists("admin:access", "Access to admin functionality");
        
        // Create default roles
        Role adminRole = createRoleIfNotExists("ADMIN", "Administrator with all permissions");
        Role instructorRole = createRoleIfNotExists("INSTRUCTOR", "Teacher who manages courses");
        Role studentRole = createRoleIfNotExists("STUDENT", "Student who enrolls in courses");
        
        // Assign all permissions to admin role
        Set<Permission> adminPermissions = new HashSet<>(permissionRepository.findAll());
        adminRole.setPermissions(adminPermissions);
        roleRepository.save(adminRole);
        
        // Assign instructor permissions
        Set<Permission> instructorPermissions = new HashSet<>();
        addPermissionIfExists(instructorPermissions, "course:create");
        addPermissionIfExists(instructorPermissions, "course:read");
        addPermissionIfExists(instructorPermissions, "course:update");
        addPermissionIfExists(instructorPermissions, "user:read");
        instructorRole.setPermissions(instructorPermissions);
        roleRepository.save(instructorRole);
        
        // Assign student permissions
        Set<Permission> studentPermissions = new HashSet<>();
        addPermissionIfExists(studentPermissions, "course:read");
        addPermissionIfExists(studentPermissions, "enrollment:create");
        addPermissionIfExists(studentPermissions, "enrollment:read");
        addPermissionIfExists(studentPermissions, "enrollment:delete");
        studentRole.setPermissions(studentPermissions);
        roleRepository.save(studentRole);
        
        // Initialize security endpoints
        initializeSecurityEndpoints();
        
        log.info("Security data initialization completed");
    }
    
    @Transactional
    private void initializeSecurityEndpoints() {
        if (securityEndpointRepository.count() == 0) {
            log.info("Initializing security endpoints...");
            
            // Department endpoints
            mapEndpointToPermission("GET", "/api/departments/**", "department:read");
            mapEndpointToPermission("POST", "/api/departments/**", "department:create");
            mapEndpointToPermission("PUT", "/api/departments/**", "department:update");
            mapEndpointToPermission("DELETE", "/api/departments/**", "department:delete");
            
            // Course endpoints
            mapEndpointToPermission("GET", "/api/courses/**", "course:read");
            mapEndpointToPermission("POST", "/api/courses/**", "course:create");
            mapEndpointToPermission("PUT", "/api/courses/**", "course:update");
            mapEndpointToPermission("DELETE", "/api/courses/**", "course:delete");
            
            // User endpoints
            mapEndpointToPermission("GET", "/api/v1/users/**", "user:read");
            mapEndpointToPermission("POST", "/api/v1/users/**", "user:create");
            mapEndpointToPermission("PUT", "/api/v1/users/**", "user:update");
            mapEndpointToPermission("DELETE", "/api/v1/users/**", "user:delete");
            
            // Admin endpoints
            mapEndpointToPermission("GET", "/api/admin/**", "admin:access");
            mapEndpointToPermission("POST", "/api/admin/**", "admin:access");
            mapEndpointToPermission("PUT", "/api/admin/**", "admin:access");
            mapEndpointToPermission("DELETE", "/api/admin/**", "admin:access");
            
            log.info("Security endpoints initialization completed");
        }
    }
    
    private Permission createPermissionIfNotExists(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(name);
                    permission.setDescription(description);
                    permission.setActive(true);
                    log.info("Creating permission: {}", name);
                    return permissionRepository.save(permission);
                });
    }
    
    private Role createRoleIfNotExists(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setActive(true);
                    log.info("Creating role: {}", name);
                    return roleRepository.save(role);
                });
    }
    
    private void addPermissionIfExists(Set<Permission> permissions, String permissionName) {
        permissionRepository.findByName(permissionName).ifPresent(permissions::add);
    }
    
    private void mapEndpointToPermission(String method, String pathPattern, String permissionName) {
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
        
        SecurityEndpoint endpoint = new SecurityEndpoint();
        endpoint.setHttpMethod(method);
        endpoint.setPathPattern(pathPattern);
        endpoint.setRequiredPermission(permission);
        
        securityEndpointRepository.save(endpoint);
        log.info("Mapped endpoint: {} {} to permission: {}", method, pathPattern, permissionName);
    }
}