package com.example.lms.certificate.mapper;

import com.example.lms.certificate.dto.CertificateDTO;
import com.example.lms.certificate.model.Certificate;

public class CertificateMapper {
    
    public static CertificateDTO toDTO(Certificate certificate) {
        return new CertificateDTO(
                certificate.getStudentId(),
                certificate.getCourseId(),
                certificate.getCertificateUrl(),
                certificate.getIssuedAt()
        );
    }

    public static Certificate toEntity(CertificateDTO dto) {
        return new Certificate(dto.getStudentId(), dto.getCourseId(), dto.getCertificateUrl());
    }
}
