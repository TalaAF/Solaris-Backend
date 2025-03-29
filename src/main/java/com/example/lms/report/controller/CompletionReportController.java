package com.example.lms.report.controller;

import com.example.lms.report.assembler.CompletionReportAssembler;
import com.example.lms.report.dto.CompletionReportDTO;
import com.example.lms.report.model.CompletionReport;
import com.example.lms.report.service.CompletionReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports/completion")
@Tag(name = "Completion Reports", description = "API endpoints for managing course completion reports")
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
    @Operation(
        summary = "Generate completion report", 
        description = "Creates a completion report for a specific student and course",
        tags = {"Completion Reports"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Report generated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompletionReportDTO.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Student or course not found")
    })
    public ResponseEntity<CompletionReportDTO> generateReport(
            @Parameter(description = "ID of the student") @RequestParam Long studentId, 
            @Parameter(description = "ID of the course") @RequestParam Long courseId,
            @Parameter(description = "Current progress percentage (0-100)") @RequestParam double progress,
            @Parameter(description = "Whether the course is completed") @RequestParam(defaultValue = "false") boolean isCompleted) {
        
        CompletionReport report = reportService.generateReport(studentId, courseId, progress, isCompleted);
        return ResponseEntity.ok(reportAssembler.toDTO(report));
    }

    /**
     * Generate batch reports for all students in a course
     */
    @PostMapping("/generate/batch/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(
        summary = "Generate batch reports", 
        description = "Creates completion reports for all students enrolled in a specific course",
        tags = {"Completion Reports"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Batch reports generated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<List<CompletionReportDTO>> generateBatchReports(
            @Parameter(description = "ID of the course") @PathVariable Long courseId) {
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
    @Operation(
        summary = "Get student reports", 
        description = "Retrieves all completion reports for a specific student",
        tags = {"Completion Reports"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Reports retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Student not found")
    })
    public ResponseEntity<List<CompletionReportDTO>> getStudentReports(
            @Parameter(description = "ID of the student") @PathVariable Long studentId) {
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
    @Operation(
        summary = "Get latest report", 
        description = "Retrieves the latest completion report for a specific student and course",
        tags = {"Completion Reports"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Report retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompletionReportDTO.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<CompletionReportDTO> getLatestReport(
            @Parameter(description = "ID of the student") @PathVariable Long studentId, 
            @Parameter(description = "ID of the course") @PathVariable Long courseId) {
        
        CompletionReport report = reportService.getLatestReport(studentId, courseId);
        return ResponseEntity.ok(reportAssembler.toDTO(report));
    }
}