package com.example.lms.user.service;

import com.example.lms.Department.model.Department;
import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.security.model.Role;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.dto.BatchUserImportDTO;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchImportService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> importUsers(List<BatchUserImportDTO> users) {
        List<User> createdUsers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // Get default password
        String defaultPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        // Get default role
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Default STUDENT role not found"));
        
        // Process each user
        for (int i = 0; i < users.size(); i++) {
            BatchUserImportDTO dto = users.get(i);
            try {
                // Validate email
                if (userRepository.existsByEmail(dto.getEmail())) {
                    errors.add("Row " + (i + 1) + ": Email already exists: " + dto.getEmail());
                    continue;
                }
                
                // Create user
                User user = new User();
                user.setEmail(dto.getEmail());
                user.setFullName(dto.getFullName());
                user.setPassword(encodedPassword);
                user.setActive(true);
                
                // Assign roles
                Set<Role> roles = new HashSet<>();
                if (dto.getRoleNames() != null && !dto.getRoleNames().isEmpty()) {
                    for (String roleName : dto.getRoleNames()) {
                        roleRepository.findByName(roleName).ifPresent(roles::add);
                    }
                }
                
                // If no roles assigned, use default
                if (roles.isEmpty()) {
                    roles.add(studentRole);
                }
                user.setRoles(roles);
                
                // Assign department if provided
                if (dto.getDepartmentId() != null) {
                    Optional<Department> department = departmentRepository.findById(dto.getDepartmentId());
                    department.ifPresent(user::setDepartment);
                }
                
                // Save user
                user = userRepository.save(user);
                createdUsers.add(user);
                
            } catch (Exception e) {
                log.error("Error importing user at row " + (i + 1), e);
                errors.add("Row " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        // Prepare response
        Map<String, Object> result = new HashMap<>();
        result.put("usersCreated", createdUsers.size());
        result.put("defaultPassword", defaultPassword);
        result.put("errors", errors);
        
        return result;
    }
    
    private String generateRandomPassword() {
        // Generate a secure random password (8 characters with mixed case, numbers, and symbols)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}