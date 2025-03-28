package com.example.lms.certificate.service;

import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.repository.CertificateRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.user.repository.UserRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import io.jsonwebtoken.io.IOException;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

import java.util.Optional;

import com.example.lms.user.model.User;
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
    public Certificate getCertificateByStudentAndCourse(Long studentId, Long courseId) {
        return certificateRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
    }
    
    public boolean certificateExists(Long studentId, Long courseId) {
        return certificateRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent();
    }
      // Add to CertificateService.java
@Transactional(readOnly = true)
public boolean verifyCertificate(String certificateId) {
    return certificateRepository.existsByVerificationId(certificateId);
}
@Transactional
public Certificate generateCertificateWithTemplate(Long studentId, Long courseId, String templateId) {
    // Get template details
    String certificateUrl = "https://lms.com/certificates/" + studentId + "/" + courseId + "?template=" + templateId;

    // Generate certificate with the template
    Certificate certificate = new Certificate(studentId, courseId, certificateUrl);
    return certificateRepository.save(certificate);
}
@Autowired
private UserRepository userRepository;

@Autowired
private CourseRepository courseRepository;

@Transactional
public byte[] generateCertificatePDF(Long certificateId) throws IOException, com.itextpdf.text.DocumentException {
    Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    
    User student = userRepository.findById(certificate.getStudentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    
    Course course = courseRepository.findById(certificate.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    
    // Use a PDF library like iText to generate a certificate
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document();
    PdfWriter.getInstance(document, baos);
    document.open();
    
    // Add certificate content
    document.add(new Paragraph("Certificate of Completion"));
    document.add(new Paragraph("This is to certify that " + student.getFullName() + 
                               " has successfully completed the course " + course.getTitle()));
    // Add more styling and content
    
    document.close();
        return baos.toByteArray(); 
}
@Transactional
public Certificate revokeCertificate(Long certificateId, String reason) {
    Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    
    certificate.setRevoked(true);
    certificate.setRevocationReason(reason);
    
    return certificateRepository.save(certificate);
}
}
