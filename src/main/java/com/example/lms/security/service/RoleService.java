package com.example.lms.security.service;

import com.example.lms.security.dto.PermissionDTO;
import com.example.lms.security.dto.RoleDTO;
import com.example.lms.security.model.Permission;
import com.example.lms.security.model.Role;
import com.example.lms.security.repository.PermissionRepository;
import com.example.lms.security.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        if (!role.isActive()) {
            throw new EntityNotFoundException("Role not found or inactive");
        }
        return mapToDto(role);
    }
    
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByNameAndActiveTrue(roleDTO.getName())) {
            throw new IllegalArgumentException("Role name already exists");
        }
        
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setActive(true);
        
        // Assign permissions if provided
        if (roleDTO.getPermissions() != null && !roleDTO.getPermissions().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (PermissionDTO permDto : roleDTO.getPermissions()) {
                Permission permission = permissionRepository.findById(permDto.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permDto.getId()));
                if (!permission.isActive()) {
                    throw new IllegalArgumentException("Cannot assign inactive permission: " + permission.getName());
                }
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }
        
        Role savedRole = roleRepository.save(role);
        return mapToDto(savedRole);
    }
    
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        if (!role.isActive()) {
            throw new EntityNotFoundException("Cannot update inactive role");
        }
        
        // Check if name is being changed and if it already exists
        if (!role.getName().equals(roleDTO.getName()) && 
                roleRepository.existsByNameAndActiveTrue(roleDTO.getName())) {
            throw new IllegalArgumentException("Role name already exists");
        }
        
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        
        // Update permissions if provided
        if (roleDTO.getPermissions() != null) {
            Set<Permission> permissions = new HashSet<>();
            for (PermissionDTO permDto : roleDTO.getPermissions()) {
                Permission permission = permissionRepository.findById(permDto.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permDto.getId()));
                if (!permission.isActive()) {
                    throw new IllegalArgumentException("Cannot assign inactive permission: " + permission.getName());
                }
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }
        
        Role updatedRole = roleRepository.save(role);
        return mapToDto(updatedRole);
    }
    
    @Transactional
    public RoleDTO assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        if (!role.isActive()) {
            throw new EntityNotFoundException("Cannot update inactive role");
        }
        
        Set<Permission> permissions = new HashSet<>();
        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));
            if (!permission.isActive()) {
                throw new IllegalArgumentException("Cannot assign inactive permission: " + permission.getName());
            }
            permissions.add(permission);
        }
        
        // Add new permissions to existing ones
        role.getPermissions().addAll(permissions);
        
        Role updatedRole = roleRepository.save(role);
        return mapToDto(updatedRole);
    }
    
    @Transactional
    public RoleDTO removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        if (!role.isActive()) {
            throw new EntityNotFoundException("Cannot update inactive role");
        }
        
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));
        
        role.getPermissions().remove(permission);
        
        Role updatedRole = roleRepository.save(role);
        return mapToDto(updatedRole);
    }
    
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        // Soft delete by setting active to false
        role.setActive(false);
        roleRepository.save(role);
    }
    
    @Transactional
    public RoleDTO reactivateRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        
        role.setActive(true);
        Role reactivatedRole = roleRepository.save(role);
        return mapToDto(reactivatedRole);
    }
    
    private RoleDTO mapToDto(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        
        Set<PermissionDTO> permissionDTOs = role.getPermissions().stream()
                .filter(Permission::isActive)  // Only include active permissions
                .map(permission -> new PermissionDTO(
                        permission.getId(),
                        permission.getName(),
                        permission.getDescription()
                ))
                .collect(Collectors.toSet());
        
        dto.setPermissions(permissionDTOs);
        return dto;
    }
}