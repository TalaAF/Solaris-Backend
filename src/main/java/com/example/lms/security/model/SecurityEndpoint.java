package com.example.lms.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "security_endpoints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String httpMethod; // GET, POST, PUT, DELETE, etc.
    
    @Column(nullable = false)
    private String pathPattern; // e.g., /api/departments/**
    
    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission requiredPermission;
}