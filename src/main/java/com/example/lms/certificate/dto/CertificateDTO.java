package com.example.lms.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;  // Add this import
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor // This will generate a default constructor
public class CertificateDTO {
    private Long id;
    private Long studentId;
    private Long courseId;
    private String courseName;
    private String certificateUrl;
    private String verificationId;
    private LocalDateTime issuedAt;
    private boolean revoked;
    private String revocationReason;
}
