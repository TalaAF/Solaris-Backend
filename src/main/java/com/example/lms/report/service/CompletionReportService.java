package com.example.lms.report.service;

import com.example.lms.report.model.CompletionReport;
import com.example.lms.report.repository.CompletionReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompletionReportService {

    @Autowired
    private CompletionReportRepository reportRepository;

    public CompletionReport generateReport(Long studentId, Long courseId, double progress, boolean isCompleted) {
        CompletionReport report = new CompletionReport(studentId, courseId, progress, isCompleted);
        return reportRepository.save(report);
    }

    public List<CompletionReport> getReportsForStudent(Long studentId) {
        return reportRepository.findByStudentId(studentId);
    }
}
