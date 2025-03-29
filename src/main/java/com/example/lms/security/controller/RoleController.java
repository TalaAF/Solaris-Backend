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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// RoleController.java
@RestController
@RequestMapping("/api/admin/roles")
@Tag(name = "Role Management", description = "API endpoints for managing roles")
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
    @Operation(summary = "Get role by ID", description = "Retrieve a role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role found",
                    content = @Content(schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }
    
    @PostMapping
    @Operation(summary = "Create a new role", description = "Create a new role with the specified details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully",
                    content = @Content(schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        return new ResponseEntity<>(roleService.createRole(roleDTO), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing role", description = "Update the details of an existing role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully",
                    content = @Content(schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.updateRole(id, roleDTO));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role", description = "Delete a role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{roleId}/permissions")
    @Operation(summary = "Assign permissions to a role", description = "Assign a list of permissions to a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions assigned successfully",
                    content = @Content(schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Role or permissions not found")
    })
    public ResponseEntity<RoleDTO> assignPermissionsToRole(
            @PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        return ResponseEntity.ok(roleService.assignPermissionsToRole(roleId, permissionIds));
    }
    
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Remove permission from a role", description = "Remove a specific permission from a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission removed successfully",
                    content = @Content(schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Role or permission not found")
    })
    
    public ResponseEntity<RoleDTO> removePermissionFromRole(
            @PathVariable Long roleId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.removePermissionFromRole(roleId, permissionId));
    }
    
    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate a role", description = "Reactivate a role by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role reactivated successfully",
                    content = @Content(schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<RoleDTO> reactivateRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.reactivateRole(id));
    }
}
