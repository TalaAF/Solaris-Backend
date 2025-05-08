package com.example.lms.certificate.mapper;

import com.example.lms.certificate.dto.CertificateDTO;
import com.example.lms.certificate.model.Certificate;

public class CertificateMapper {
    
    public static CertificateDTO toDTO(Certificate certificate) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(certificate.getId());
        dto.setStudentId(certificate.getStudentId());
        dto.setCourseId(certificate.getCourseId());
        dto.setCourseName(certificate.getCourseName());
        dto.setCertificateUrl(certificate.getCertificateUrl());
        dto.setVerificationId(certificate.getVerificationId());
        dto.setIssuedAt(certificate.getIssuedAt());
        dto.setRevoked(certificate.isRevoked());
        return dto;
    }

    public static Certificate toEntity(CertificateDTO dto) {
        Certificate certificate = new Certificate();
        certificate.setStudentId(dto.getStudentId());
        certificate.setCourseId(dto.getCourseId());
        certificate.setCertificateUrl(dto.getCertificateUrl());
        // Only set other fields if they're allowed to be modified
        return certificate;
    }
}
