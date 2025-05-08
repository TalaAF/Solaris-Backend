package com.example.lms.certificate.controller;

import com.example.lms.certificate.dto.CertificateDTO;
import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for certificate management
 */
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /**
     * Generate a certificate for a student who completed a course
     */
    @PostMapping("/generate/{studentId}/{courseId}")
    @Operation(
        summary = "Generate a certificate for a student",
        description = "Creates a new certificate for a student who has completed a course"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificate successfully generated", 
                     content = @Content(schema = @Schema(implementation = CertificateDTO.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
        @ApiResponse(responseCode = "404", description = "Student or course not found"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access or course not completed")
    })
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<CertificateDTO> generateCertificate(
            @Parameter(description = "ID of the student") @PathVariable Long studentId,
            @Parameter(description = "ID of the course") @PathVariable Long courseId) {
        Certificate certificate = certificateService.generateCertificate(studentId, courseId);
        return ResponseEntity.ok(certificateAssembler.toDTO(certificate));
    }

    /**
     * Get certificate by ID
     */
    @GetMapping("/{certificateId}")
    @Operation(
        summary = "Get certificate by ID",
        description = "Retrieves a certificate by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificate found", 
                     content = @Content(schema = @Schema(implementation = CertificateDTO.class))),
        @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<CertificateDTO> getCertificateById(
            @Parameter(description = "ID of the certificate") @PathVariable Long certificateId) {
        Certificate certificate = certificateService.getCertificateById(certificateId);
        return ResponseEntity.ok(certificateAssembler.toDTO(certificate));
    }

    /**
     * Get certificates by student
     */
    @GetMapping("/student/{studentId}")
    @Operation(
        summary = "Get certificates by student",
        description = "Retrieves all certificates for a specific student"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of certificates"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByStudent(
            @Parameter(description = "ID of the student") @PathVariable Long studentId) {
        List<Certificate> certificates = certificateService.getCertificatesByStudent(studentId);
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificateAssembler::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(certificateDTOs);
    }

    /**
     * Get certificates by course
     */
    @GetMapping("/course/{courseId}")
    @Operation(
        summary = "Get certificates by course",
        description = "Retrieves all certificates issued for a specific course"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of certificates"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CertificateDTO>> getCertificatesByCourse(
            @Parameter(description = "ID of the course") @PathVariable Long courseId) {
        List<Certificate> certificates = certificateService.getCertificatesByCourse(courseId);
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificateAssembler::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(certificateDTOs);
    }

    /**
     * Get all certificates
     */
    @GetMapping
    @Operation(
        summary = "Get all certificates",
        description = "Retrieves all certificates in the system"
    )
    @ApiResponse(responseCode = "200", description = "List of all certificates")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CertificateDTO>> getAllCertificates() {
        List<Certificate> certificates = certificateService.getAllCertificates();
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificateAssembler::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(certificateDTOs);
    }
    
    /**
     * Verify a certificate
     */
    @GetMapping("/verify/{verificationId}")
    @Operation(
        summary = "Verify certificate",
        description = "Verifies the authenticity of a certificate using its verification ID"
    )
    @ApiResponse(responseCode = "200", description = "Certificate verification result")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> verifyCertificate(
            @Parameter(description = "Verification ID of the certificate") @PathVariable String verificationId) {
        boolean isValid = certificateService.verifyCertificate(verificationId);
        return ResponseEntity.ok(isValid);
    }
    
    /**
     * Download certificate as PDF
     */
    @GetMapping("/{certificateId}/download")
    @Operation(
        summary = "Download certificate",
        description = "Downloads a certificate as a PDF file"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificate PDF file", 
                     content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "404", description = "Certificate not found"),
        @ApiResponse(responseCode = "500", description = "Error generating PDF")
    })
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadCertificate(
            @Parameter(description = "ID of the certificate") @PathVariable Long certificateId) {
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
    
    /**
     * Generate certificates for multiple students
     */
    @PostMapping("/batch/{courseId}")
    @Operation(
        summary = "Generate batch certificates",
        description = "Generates certificates for multiple students in a course"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificates successfully generated"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<List<CertificateDTO>> generateBatchCertificates(
            @Parameter(description = "ID of the course") @PathVariable Long courseId,
            @Parameter(description = "List of student IDs") @RequestBody List<Long> studentIds) {
        
        List<Certificate> certificates = certificateService.generateBatchCertificates(courseId, studentIds);
        
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificateAssembler::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(certificateDTOs);
    }
}