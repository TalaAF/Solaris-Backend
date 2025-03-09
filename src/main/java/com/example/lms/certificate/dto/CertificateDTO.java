package com.example.lms.certificate.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class CertificateDTO {
    private Long studentId;
    private Long courseId;
    private String certificateUrl;
    private LocalDateTime issuedAt;

    public CertificateDTO(Long studentId, Long courseId, String certificateUrl, LocalDateTime issuedAt) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.certificateUrl = certificateUrl;
        this.issuedAt = issuedAt;
    }

}
