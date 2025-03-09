package com.example.lms.certificate.controller;

import com.example.lms.certificate.assembler.CertificateAssembler;
import com.example.lms.certificate.dto.CertificateDTO;
import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
