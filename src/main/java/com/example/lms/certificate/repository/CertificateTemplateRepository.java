package com.example.lms.certificate.repository;

import com.example.lms.certificate.model.CertificateTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {
    Page<CertificateTemplate> findByCourseId(Long courseId, Pageable pageable);
    Page<CertificateTemplate> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<CertificateTemplate> findByIsActive(boolean isActive, Pageable pageable);
    List<CertificateTemplate> findByNameContainingIgnoreCase(String search);
}