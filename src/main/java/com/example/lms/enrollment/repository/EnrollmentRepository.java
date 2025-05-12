package com.example.lms.enrollment.repository;

import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.enrollment.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Find enrollment by student and course
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Find all enrollments for a student
    List<Enrollment> findByStudentId(Long studentId);

    // Find all enrollments for a course
    List<Enrollment> findByCourseId(Long courseId);

    // Find enrollments by status
    List<Enrollment> findByStatus(EnrollmentStatus status);
    
    // Find completed enrollments for a student
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);
    
    // Find enrollments that are about to expire
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'APPROVED' AND e.lastAccessedDate < :cutoffDate")
    List<Enrollment> findInactiveEnrollments(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count active enrollments for a course
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status IN ('APPROVED', 'IN_PROGRESS')")
    Long countActiveEnrollmentsByCourse(@Param("courseId") Long courseId);
    
    // Check if student is enrolled in any course in a department
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.student.id = :studentId AND e.course.department.id = :departmentId AND e.status IN ('APPROVED', 'IN_PROGRESS', 'COMPLETED')")
    boolean isStudentEnrolledInDepartment(@Param("studentId") Long studentId, @Param("departmentId") Long departmentId);
    
    // Find enrollments in a specific date range
    List<Enrollment> findByEnrollmentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count enrollments by course ID
    long countByCourseId(Long courseId);

    // Count enrollments by course ID and status
    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
}