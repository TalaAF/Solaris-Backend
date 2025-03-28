package com.example.lms.certificate.controller;

import com.example.lms.certificate.assembler.CertificateAssembler;
import com.example.lms.certificate.dto.CertificateDTO;
import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.service.CertificateService;

import jakarta.transaction.Transactional;

import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateAssembler certificateAssembler;

    
    @PostMapping("/generate/{studentId}/{courseId}")
    public ResponseEntity<CertificateDTO> generateCertificate(@PathVariable Long studentId, @PathVariable Long courseId) {
        Certificate certificate = certificateService.generateCertificate(studentId, courseId);
        return ResponseEntity.ok(certificateAssembler.toDTO(certificate));
    }

   
    @GetMapping("/{certificateId}")
    public ResponseEntity<CertificateDTO> getCertificateById(@PathVariable Long certificateId) {
        Certificate certificate = certificateService.getCertificateById(certificateId);
        return ResponseEntity.ok(certificateAssembler.toDTO(certificate));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByStudent(@PathVariable Long studentId) {
        List<Certificate> certificates = certificateService.getCertificatesByStudent(studentId);
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificateAssembler::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(certificateDTOs);
    }


    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByCourse(@PathVariable Long courseId) {
        List<Certificate> certificates = certificateService.getCertificatesByCourse(courseId);
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificateAssembler::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(certificateDTOs);
    }


    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getAllCertificates() {
        List<Certificate> certificates = certificateService.getAllCertificates();
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificateAssembler::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(certificateDTOs);
    }
    

// Add endpoint to CertificateController.java
@GetMapping("/verify/{verificationId}")
public ResponseEntity<Boolean> verifyCertificate(@PathVariable String verificationId) {
    boolean isValid = certificateService.verifyCertificate(verificationId);
    return ResponseEntity.ok(isValid);
}
@GetMapping("/{certificateId}/download")
public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long certificateId) {
    try {
        byte[] certificatePdf = certificateService.generateCertificatePDF(certificateId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate.pdf");
        
        return new ResponseEntity<>(certificatePdf, headers, HttpStatus.OK);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
}
