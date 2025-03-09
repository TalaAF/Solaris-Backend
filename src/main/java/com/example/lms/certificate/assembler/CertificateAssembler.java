package com.example.lms.certificate.assembler;

import com.example.lms.certificate.dto.CertificateDTO;
import com.example.lms.certificate.model.Certificate;
import com.example.lms.certificate.mapper.CertificateMapper;
import org.springframework.stereotype.Component;

@Component
public class CertificateAssembler {

    public CertificateDTO toDTO(Certificate certificate) {
        return CertificateMapper.toDTO(certificate);
    }

    public Certificate toEntity(CertificateDTO dto) {
        return CertificateMapper.toEntity(dto);
    }
}
