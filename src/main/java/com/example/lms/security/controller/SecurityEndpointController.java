package com.example.lms.security.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lms.security.dto.SecurityEndpointDTO;
import com.example.lms.security.service.SecurityEndpointService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

// SecurityEndpointController.java
@RestController
@RequestMapping("/api/admin/security-endpoints")
@Tag(name = "Security Endpoint Management", description = "API endpoints for managing security endpoints")
public class SecurityEndpointController {
    
    private final SecurityEndpointService securityEndpointService;
    
    public SecurityEndpointController(SecurityEndpointService securityEndpointService) {
        this.securityEndpointService = securityEndpointService;
    }
    
    @GetMapping
    @Operation(summary = "Get all security endpoints", description = "Retrieve a list of all security endpoints")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Security endpoints retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SecurityEndpointDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SecurityEndpointDTO>> getAllSecurityEndpoints() {
        return ResponseEntity.ok(securityEndpointService.getAllSecurityEndpoints());
    }
    
    @PostMapping
    @Operation(summary = "Create a new security endpoint", description = "Create a new security endpoint with the specified details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Security endpoint created successfully",
                    content = @Content(schema = @Schema(implementation = SecurityEndpointDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<SecurityEndpointDTO> createSecurityEndpoint(
            @Valid @RequestBody SecurityEndpointDTO dto) {
        return new ResponseEntity<>(securityEndpointService.createSecurityEndpoint(dto), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing security endpoint", description = "Update the details of an existing security endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Security endpoint updated successfully",
                    content = @Content(schema = @Schema(implementation = SecurityEndpointDTO.class))),
            @ApiResponse(responseCode = "404", description = "Security endpoint not found"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<SecurityEndpointDTO> updateSecurityEndpoint(
            @PathVariable Long id, @Valid @RequestBody SecurityEndpointDTO dto) {
        return ResponseEntity.ok(securityEndpointService.updateSecurityEndpoint(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a security endpoint", description = "Delete a security endpoint by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Security endpoint deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Security endpoint not found")
    })
    public ResponseEntity<Void> deleteSecurityEndpoint(@PathVariable Long id) {
        securityEndpointService.deleteSecurityEndpoint(id);
        return ResponseEntity.noContent().build();
    }
}
