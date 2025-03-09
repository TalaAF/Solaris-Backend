package com.example.lms.report.repository;

import com.example.lms.report.model.CompletionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompletionReportRepository extends JpaRepository<CompletionReport, Long> {
    List<CompletionReport> findByStudentId(Long studentId);
}
