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

    private Long studentId;
    private Long courseId;
    private String certificateUrl;
    private LocalDateTime issuedAt;

    public Certificate() {}

    public Certificate(Long studentId, Long courseId, String certificateUrl) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.certificateUrl = certificateUrl;
        this.issuedAt = LocalDateTime.now();
    }

}
