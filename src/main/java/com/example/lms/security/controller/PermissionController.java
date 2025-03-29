package com.example.lms.security.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lms.security.dto.PermissionDTO;
import com.example.lms.security.service.PermissionService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RequestBody;
// PermissionController.java
@RestController
@RequestMapping("/api/admin/permissions")
@Tag(name = "Permission Management", description = "API endpoints for managing permissions")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID", description = "Retrieve a permission by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission found",
                    content = @Content(schema = @Schema(implementation = PermissionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }
    
    @PostMapping
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        return new ResponseEntity<>(permissionService.createPermission(permissionDTO), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update permission", description = "Update an existing permission by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission updated",
                    content = @Content(schema = @Schema(implementation = PermissionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    public ResponseEntity<PermissionDTO> updatePermission(
            @PathVariable Long id, @Valid @RequestBody PermissionDTO permissionDTO) {
        return ResponseEntity.ok(permissionService.updatePermission(id, permissionDTO));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission", description = "Delete a permission by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Permission deleted"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
