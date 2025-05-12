package com.example.lms.certificate.repository;

import com.example.lms.certificate.model.CertificateTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {
    
    // Find all active templates
    Page<CertificateTemplate> findByIsActive(boolean isActive, Pageable pageable);
    
    // Find by semester name
    List<CertificateTemplate> findBySemesterNameContainingIgnoreCase(String semesterName);
    
    // Search by name or description
    @Query("SELECT ct FROM CertificateTemplate ct WHERE " +
           "LOWER(ct.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ct.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ct.semesterName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CertificateTemplate> searchTemplates(@Param("searchTerm") String searchTerm);
}