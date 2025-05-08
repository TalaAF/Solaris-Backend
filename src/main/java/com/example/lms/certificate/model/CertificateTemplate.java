package com.example.lms.certificate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_templates")
@Data
@NoArgsConstructor
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Long courseId;
    
    private String courseName;
    
    private Long departmentId;
    
    private String departmentName;
    
    @Column(length = 10000)
    private String templateContent;
    
    private boolean isActive = true;
    
    private int issuedCount = 0;
    
    @Column(nullable = false)
    private LocalDateTime dateCreated;
    
    private LocalDateTime lastModified;
    
    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.lastModified = LocalDateTime.now();
    }
}