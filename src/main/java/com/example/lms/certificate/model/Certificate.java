package com.example.lms.certificate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String templateId;
    private Long studentId;
    private Long courseId;
    private String certificateUrl;
    private LocalDateTime issuedAt;
    private String courseName;

    @Column(unique = true)  // Assuming verification IDs should be unique
    private String verificationId;
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
@Column
private String linkedInSharingUrl;
 

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
