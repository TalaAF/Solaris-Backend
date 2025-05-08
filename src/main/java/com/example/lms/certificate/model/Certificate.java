package com.example.lms.certificate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "certificates")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long studentId;
    private Long courseId;
    private String courseName;
    private String certificateUrl;
    private String verificationId;
    private LocalDateTime issuedAt;
    
    private boolean revoked; // Add this field
    private String template; // Add this field
    
    // Additional fields
    private String achievementDetails;
    private Integer grade;
    private String issuerName;
    private String issuerSignature;
    private String linkedInSharingUrl;
    
    // Make sure these methods are available
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
    
    public boolean isRevoked() {
        return this.revoked;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public String getTemplate() {
        return this.template;
    }

    public Certificate(Long studentId, Long courseId, String certificateUrl , String courseName) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.certificateUrl = certificateUrl;
        this.issuedAt = LocalDateTime.now();
        this.courseName= courseName;
    }
    public Certificate(Long studentId, Long courseId, String certificateUrl) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.certificateUrl = certificateUrl;}
}
