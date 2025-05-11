package com.example.lms.certificate.service;

import com.example.lms.certificate.model.CertificateTemplate;
import com.example.lms.certificate.repository.CertificateTemplateRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateTemplateService {

    private final CertificateTemplateRepository templateRepository;

    public Page<CertificateTemplate> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }

    public List<CertificateTemplate> getTemplatesBySemesterName(String semesterName) {
        return templateRepository.findBySemesterNameContainingIgnoreCase(semesterName);
    }

    public CertificateTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
    }

    public Page<CertificateTemplate> getActiveTemplates(Pageable pageable) {
        return templateRepository.findByIsActive(true, pageable);
    }

    public List<CertificateTemplate> searchTemplates(String searchTerm) {
        return templateRepository.searchTemplates(searchTerm);
    }

    @Transactional
    public CertificateTemplate createTemplate(CertificateTemplate template) {
        template.setDateCreated(LocalDateTime.now());
        template.setLastModified(LocalDateTime.now());
        return templateRepository.save(template);
    }

    @Transactional
    public CertificateTemplate updateTemplate(Long id, CertificateTemplate templateDetails) {
        CertificateTemplate template = getTemplateById(id);
        
        template.setName(templateDetails.getName());
        template.setDescription(templateDetails.getDescription());
        template.setSemesterName(templateDetails.getSemesterName());
        template.setTemplateContent(templateDetails.getTemplateContent());
        template.setActive(templateDetails.isActive());
        template.setLastModified(LocalDateTime.now());
        
        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        CertificateTemplate template = getTemplateById(id);
        templateRepository.delete(template);
    }

    @Transactional
    public void incrementIssuedCount(Long id) {
        CertificateTemplate template = getTemplateById(id);
        template.setIssuedCount(template.getIssuedCount() + 1);
        template.setLastModified(LocalDateTime.now());
        templateRepository.save(template);
    }
}