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
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import com.example.lms.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    public Certificate generateCertificate(Long studentId, Long courseId) {
        log.info("Generating certificate for student ID: {} and course ID: {}", studentId, courseId);
        // Validate student
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        
        // Validate course
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        // Check if certificate already exists
        if (certificateExists(studentId, courseId)) {
            throw new IllegalStateException("Certificate already exists for student " + studentId + " in course " + courseId);
        }
    
        String certificateUrl = "https://lms.com/certificates/" + studentId + "/" + courseId;
        Certificate certificate = new Certificate(studentId, courseId, certificateUrl, courseRepository.findById(courseId).get().getTitle());
        log.info("Certificate URL: {}", certificateUrl);
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
    document.add(new Paragraph("Certificate of Completion", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD)));
    document.add(new Paragraph("\n"));
    document.add(new Paragraph("This is to certify that", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14)));
    document.add(new Paragraph(student.getFullName(), new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD)));
    document.add(new Paragraph("has successfully completed the course", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14)));
    document.add(new Paragraph(course.getTitle(), new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16)));
    document.add(new Paragraph("Issued on: " + certificate.getIssuedAt().toString(), new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12)));

    document.close();
        return baos.toByteArray(); 
}
@Transactional
public Certificate revokeCertificate(Long certificateId, String reason) {
    Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    
    certificate.setRevoked(true);
    
    return certificateRepository.save(certificate);
}
@Transactional
public List<Certificate> generateBatchCertificates(Long courseId, List<Long> studentIds) {
    List<Certificate> certificates = new ArrayList<>();
    
    for (Long studentId : studentIds) {
        certificates.add(generateCertificate(studentId, courseId));
    }
    
    return certificates;
}
@Transactional
public String generateLinkedInSharingUrl(Long certificateId) throws UnsupportedEncodingException {
    Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    
    String baseUrl = "https://www.linkedin.com/profile/add?startTask=CERTIFICATION_NAME";
    String certName;
    try {
        certName = URLEncoder.encode("Course Completion: " + certificate.getCourseName(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Encoding not supported", e);
    }
    String orgName = URLEncoder.encode("Your LMS Name", "UTF-8");
    
    String sharingUrl = baseUrl + 
                        "&name=" + certName + 
                        "&organizationName=" + orgName;
    
    certificate.setLinkedInSharingUrl(sharingUrl);
    certificateRepository.save(certificate);
    
    return sharingUrl;
}
}
