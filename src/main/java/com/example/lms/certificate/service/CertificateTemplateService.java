package com.example.lms.certificate.service;

import com.example.lms.certificate.model.CertificateTemplate;
import com.example.lms.certificate.repository.CertificateTemplateRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateTemplateService {

    private final CertificateTemplateRepository templateRepository;

    public Page<CertificateTemplate> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }

    public CertificateTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
    }

    public Page<CertificateTemplate> getTemplatesByCourse(Long courseId, Pageable pageable) {
        return templateRepository.findByCourseId(courseId, pageable);
    }

    public Page<CertificateTemplate> getTemplatesByDepartment(Long departmentId, Pageable pageable) {
        return templateRepository.findByDepartmentId(departmentId, pageable);
    }

    public Page<CertificateTemplate> getActiveTemplates(Pageable pageable) {
        return templateRepository.findByIsActive(true, pageable);
    }

    @Transactional
    public CertificateTemplate createTemplate(CertificateTemplate template) {
        return templateRepository.save(template);
    }

    @Transactional
    public CertificateTemplate updateTemplate(Long id, CertificateTemplate templateDetails) {
        CertificateTemplate template = getTemplateById(id);
        
        template.setName(templateDetails.getName());
        template.setDescription(templateDetails.getDescription());
        template.setCourseId(templateDetails.getCourseId());
        template.setCourseName(templateDetails.getCourseName());
        template.setDepartmentId(templateDetails.getDepartmentId());
        template.setDepartmentName(templateDetails.getDepartmentName());
        template.setTemplateContent(templateDetails.getTemplateContent());
        template.setActive(templateDetails.isActive());
        
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
        templateRepository.save(template);
    }

    public List<CertificateTemplate> searchTemplates(String searchTerm) {
        return templateRepository.findByNameContainingIgnoreCase(searchTerm);
    }
}