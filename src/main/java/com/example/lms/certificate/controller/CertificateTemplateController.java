package com.example.lms.certificate.controller;

import com.example.lms.certificate.model.CertificateTemplate;
import com.example.lms.certificate.service.CertificateTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificate-templates")
@RequiredArgsConstructor
public class CertificateTemplateController {

    private final CertificateTemplateService templateService;

    @GetMapping
    public ResponseEntity<Page<CertificateTemplate>> getAllTemplates(
            Pageable pageable,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search) {
        
        Page<CertificateTemplate> templates;
        
        if (courseId != null) {
            templates = templateService.getTemplatesByCourse(courseId, pageable);
        } else if (departmentId != null) {
            templates = templateService.getTemplatesByDepartment(departmentId, pageable);
        } else if (isActive != null && isActive) {
            templates = templateService.getActiveTemplates(pageable);
        } else if (search != null && !search.isEmpty()) {
            List<CertificateTemplate> searchResults = templateService.searchTemplates(search);
            // Convert list to page for consistent response
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), searchResults.size());
            templates = start < end 
                ? Page.empty(pageable)
                : new org.springframework.data.domain.PageImpl<>(
                    searchResults.subList(start, end), pageable, searchResults.size());
        } else {
            templates = templateService.getAllTemplates(pageable);
        }
        
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateTemplate> getTemplateById(@PathVariable Long id) {
        CertificateTemplate template = templateService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }

    @PostMapping
    public ResponseEntity<CertificateTemplate> createTemplate(@RequestBody CertificateTemplate template) {
        CertificateTemplate newTemplate = templateService.createTemplate(template);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTemplate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificateTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody CertificateTemplate template) {
        
        CertificateTemplate updatedTemplate = templateService.updateTemplate(id, template);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}