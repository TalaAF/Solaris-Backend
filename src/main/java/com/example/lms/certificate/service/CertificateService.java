package com.example.lms.certificate.service;

import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.repository.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    public Certificate generateCertificate(Long studentId, Long courseId) {
        String certificateUrl = "https://lms.com/certificates/" + studentId + "/" + courseId; 

        Certificate certificate = new Certificate(studentId, courseId, certificateUrl);
        return certificateRepository.save(certificate);
    }
}
