package com.example.lms.report.repository;

import com.example.lms.report.model.CompletionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompletionReportRepository extends JpaRepository<CompletionReport, Long> {
    
    List<CompletionReport> findByStudentId(Long studentId);
    
    Optional<CompletionReport> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    @Query("SELECT cr FROM CompletionReport cr WHERE cr.studentId = :studentId AND cr.reportGeneratedAt > :dateThreshold")
    List<CompletionReport> findRecentReports(
        @Param("studentId") Long studentId, 
        @Param("dateThreshold") LocalDateTime dateThreshold
    );
    
    @Query("SELECT AVG(cr.progress) FROM CompletionReport cr WHERE cr.studentId = :studentId")
    Double calculateAverageProgress(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(cr) FROM CompletionReport cr WHERE cr.studentId = :studentId AND cr.isCompleted = true")
    Long countCompletedReports(@Param("studentId") Long studentId);
}