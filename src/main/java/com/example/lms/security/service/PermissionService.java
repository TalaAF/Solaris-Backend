package com.example.lms.security.service;

import com.example.lms.security.dto.PermissionDTO;
import com.example.lms.security.model.Permission;
import com.example.lms.security.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));
        return mapToDto(permission);
    }
    
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        if (permissionRepository.existsByName(permissionDTO.getName())) {
            throw new IllegalArgumentException("Permission name already exists");
        }
        
        Permission permission = new Permission();
        permission.setName(permissionDTO.getName());
        permission.setDescription(permissionDTO.getDescription());
        
        Permission savedPermission = permissionRepository.save(permission);
        return mapToDto(savedPermission);
    }
    
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));
        
        // Check if name is being changed and if it already exists
        if (!permission.getName().equals(permissionDTO.getName()) && 
                permissionRepository.existsByName(permissionDTO.getName())) {
            throw new IllegalArgumentException("Permission name already exists");
        }
        
        permission.setName(permissionDTO.getName());
        permission.setDescription(permissionDTO.getDescription());
        
        Permission updatedPermission = permissionRepository.save(permission);
        return mapToDto(updatedPermission);
    }
    
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));
        
        // Soft delete by setting active to false
        permission.setActive(false);
        permissionRepository.save(permission);
    }
    
    private PermissionDTO mapToDto(Permission permission) {
        return new PermissionDTO(
                permission.getId(),
                permission.getName(),
                permission.getDescription()
        );
    }
}