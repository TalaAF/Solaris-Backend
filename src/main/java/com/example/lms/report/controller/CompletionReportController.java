package com.example.lms.report.controller;

import com.example.lms.report.assembler.CompletionReportAssembler;
import com.example.lms.report.dto.CompletionReportDTO;
import com.example.lms.report.model.CompletionReport;
import com.example.lms.report.service.CompletionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports/completion")
public class CompletionReportController {

    @Autowired
    private CompletionReportService reportService;

    @Autowired
    private CompletionReportAssembler reportAssembler;

    /**
     * Generate a completion report for a specific student and course
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<CompletionReportDTO> generateReport(
            @RequestParam Long studentId, 
            @RequestParam Long courseId,
            @RequestParam double progress,
            @RequestParam(defaultValue = "false") boolean isCompleted) {
        
        CompletionReport report = reportService.generateReport(studentId, courseId, progress, isCompleted);
        return ResponseEntity.ok(reportAssembler.toDTO(report));
    }

    /**
     * Generate batch reports for all students in a course
     */
    @PostMapping("/generate/batch/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CompletionReportDTO>> generateBatchReports(@PathVariable Long courseId) {
        List<CompletionReport> reports = reportService.generateBatchReports(courseId);
        
        List<CompletionReportDTO> reportDTOs = reports.stream()
                .map(reportAssembler::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(reportDTOs);
    }

    /**
     * Get all completion reports for a student
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<List<CompletionReportDTO>> getStudentReports(@PathVariable Long studentId) {
        List<CompletionReport> reports = reportService.getReportsForStudent(studentId);
        
        List<CompletionReportDTO> reportDTOs = reports.stream()
                .map(reportAssembler::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(reportDTOs);
    }

    /**
     * Get the latest report for a specific student and course
     */
    @GetMapping("/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public ResponseEntity<CompletionReportDTO> getLatestReport(
            @PathVariable Long studentId, 
            @PathVariable Long courseId) {
        
        CompletionReport report = reportService.getLatestReport(studentId, courseId);
        return ResponseEntity.ok(reportAssembler.toDTO(report));
    }

  
}