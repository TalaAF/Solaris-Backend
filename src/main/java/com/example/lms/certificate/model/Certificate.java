package com.example.lms.certificate.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String templateId;
    private Long studentId;
    private Long courseId;
    private String certificateUrl;
    private LocalDateTime issuedAt;
    @Column
private String achievementDetails;

@Column
private Integer grade;

@Column
private String issuerName;

@Column
private String issuerSignature;
@Column
private boolean isRevoked;

@Column
private String revocationReason;

    public Certificate() {}

    public Certificate(Long studentId, Long courseId, String certificateUrl) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.certificateUrl = certificateUrl;
        this.issuedAt = LocalDateTime.now();
    }

}
