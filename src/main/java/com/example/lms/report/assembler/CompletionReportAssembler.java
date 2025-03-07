package com.example.lms.report.assembler;

import com.example.lms.report.dto.CompletionReportDTO;
import com.example.lms.report.model.CompletionReport;
import com.example.lms.report.mapper.CompletionReportMapper;
import org.springframework.stereotype.Component;

@Component
public class CompletionReportAssembler {

    public CompletionReportDTO toDTO(CompletionReport report) {
        return CompletionReportMapper.toDTO(report);
    }

    public CompletionReport toEntity(CompletionReportDTO dto) {
        return CompletionReportMapper.toEntity(dto);
    }
}
