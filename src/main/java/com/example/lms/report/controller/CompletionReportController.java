package com.example.lms.report.controller;

import com.example.lms.report.assembler.CompletionReportAssembler;
import com.example.lms.report.dto.CompletionReportDTO;
import com.example.lms.report.model.CompletionReport;
import com.example.lms.report.service.CompletionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class CompletionReportController {

    @Autowired
    private CompletionReportService reportService;

    @Autowired
    private CompletionReportAssembler reportAssembler;

    @PostMapping("/generate/{studentId}/{courseId}")
    public ResponseEntity<CompletionReportDTO> generateReport(
            @PathVariable Long studentId, 
            @PathVariable Long courseId,
            @RequestParam double progress,
            @RequestParam boolean isCompleted) {

        CompletionReport report = reportService.generateReport(studentId, courseId, progress, isCompleted);
        return ResponseEntity.ok(reportAssembler.toDTO(report));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CompletionReportDTO>> getStudentReports(@PathVariable Long studentId) {
        List<CompletionReportDTO> reports = reportService.getReportsForStudent(studentId)
                .stream().map(reportAssembler::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(reports);
    }
}
