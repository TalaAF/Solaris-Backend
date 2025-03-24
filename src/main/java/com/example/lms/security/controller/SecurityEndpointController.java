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

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

// SecurityEndpointController.java
@RestController
@RequestMapping("/api/admin/security-endpoints")
public class SecurityEndpointController {
    
    private final SecurityEndpointService securityEndpointService;
    
    public SecurityEndpointController(SecurityEndpointService securityEndpointService) {
        this.securityEndpointService = securityEndpointService;
    }
    
    @GetMapping
    public ResponseEntity<List<SecurityEndpointDTO>> getAllSecurityEndpoints() {
        return ResponseEntity.ok(securityEndpointService.getAllSecurityEndpoints());
    }
    
    @PostMapping
    public ResponseEntity<SecurityEndpointDTO> createSecurityEndpoint(
            @Valid @RequestBody SecurityEndpointDTO dto) {
        return new ResponseEntity<>(securityEndpointService.createSecurityEndpoint(dto), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SecurityEndpointDTO> updateSecurityEndpoint(
            @PathVariable Long id, @Valid @RequestBody SecurityEndpointDTO dto) {
        return ResponseEntity.ok(securityEndpointService.updateSecurityEndpoint(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSecurityEndpoint(@PathVariable Long id) {
        securityEndpointService.deleteSecurityEndpoint(id);
        return ResponseEntity.noContent().build();
    }
}
