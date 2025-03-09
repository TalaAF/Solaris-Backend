package com.example.lms.security.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.example.lms.security.service.RoleService;
import jakarta.validation.Valid;
import com.example.lms.security.dto.RoleDTO;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;
// RoleController.java
@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }
    
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        return new ResponseEntity<>(roleService.createRole(roleDTO), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.updateRole(id, roleDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<RoleDTO> assignPermissionsToRole(
            @PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        return ResponseEntity.ok(roleService.assignPermissionsToRole(roleId, permissionIds));
    }
    
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleDTO> removePermissionFromRole(
            @PathVariable Long roleId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.removePermissionFromRole(roleId, permissionId));
    }
    
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<RoleDTO> reactivateRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.reactivateRole(id));
    }
}
