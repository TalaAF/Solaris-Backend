package com.example.lms.certificate.service;

import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.repository.CertificateRepository;

import java.util.List;

import java.util.Optional;
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
    
  
    public Certificate getCertificateById(Long certificateId) {
        Optional<Certificate> certificate = certificateRepository.findById(certificateId);
        return certificate.orElseThrow(() -> new RuntimeException("Certificate not found"));
    }

    
    public List<Certificate> getCertificatesByStudent(Long studentId) {
        return certificateRepository.findByStudentId(studentId);
    }


    public List<Certificate> getCertificatesByCourse(Long courseId) {
        return certificateRepository.findByCourseId(courseId);
    }

   
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }
}
