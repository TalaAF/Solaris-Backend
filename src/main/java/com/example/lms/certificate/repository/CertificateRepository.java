package com.example.lms.certificate.repository;

import com.example.lms.certificate.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByVerificationId(String verificationId); 


    List<Certificate> findByStudentId(Long studentId);
    List<Certificate> findByCourseId(Long courseId);
}

