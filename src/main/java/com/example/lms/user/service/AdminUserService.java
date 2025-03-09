package com.example.lms.user.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.security.dto.RoleDTO;
import com.example.lms.security.model.Role;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.dto.UserDTO;
import com.example.lms.user.dto.UserListDTO;
import com.example.lms.user.mapper.UserMapper;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserListDTO> getAllUsers(
            int page, int size, String sortBy, String sortDirection,
            String search, Boolean active, Set<String> roleNames) {
        
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Search by email or full name
            if (search != null && !search.isEmpty()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchLike),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), searchLike)
                ));
            }
            
            // Filter by active status
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), active));
            }
            
            // Filter by roles
            if (roleNames != null && !roleNames.isEmpty()) {
                predicates.add(root.join("roles").get("name").in(roleNames));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToUserListDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDTO activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(true);
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public UserDTO deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public UserDTO updateUserRoles(Long id, Set<String> roleNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        
        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findByActiveTrue().stream()
                .map(this::mapToRoleDTO)
                .collect(Collectors.toList());
    }

    private UserListDTO mapToUserListDTO(User user) {
        UserListDTO dto = new UserListDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        
        if (user.getRoles() != null) {
            dto.setRoleNames(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet()));
        }
        
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }
        
        return dto;
    }

    private RoleDTO mapToRoleDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}