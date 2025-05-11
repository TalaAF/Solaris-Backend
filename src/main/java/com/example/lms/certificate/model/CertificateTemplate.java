package com.example.lms.certificate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    
    @Column(name = "semester_name") // Make sure column names match the database
    private String semesterName;
    
    // Remove these if they don't exist in the database
    // private Long departmentId;  
    // private String departmentName;
    // private Long courseId;
    // private String courseName;
    
    @Column(name = "template_content", columnDefinition = "TEXT")
    private String templateContent;
    
    @Column(name = "is_active")
    private boolean isActive;
    
    @Column(name = "issued_count")
    private int issuedCount = 0;
    
    @Column(name = "date_created")
    private LocalDateTime dateCreated = LocalDateTime.now();
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified = LocalDateTime.now();
    
    // Add any missing fields that exist in the database
}