package com.example.lms.report.mapper;

import com.example.lms.report.dto.CompletionReportDTO;
import com.example.lms.report.model.CompletionReport;

public class CompletionReportMapper {
    
    public static CompletionReportDTO toDTO(CompletionReport report) {
        return new CompletionReportDTO(
                report.getStudentId(),
                report.getCourseId(),
                report.getProgress(),
                report.isCompleted(),
                report.getReportGeneratedAt()
        );
    }

    public static CompletionReport toEntity(CompletionReportDTO dto) {
        return new CompletionReport(dto.getStudentId(), dto.getCourseId(), dto.getProgress(), dto.isCompleted());
    }
}
