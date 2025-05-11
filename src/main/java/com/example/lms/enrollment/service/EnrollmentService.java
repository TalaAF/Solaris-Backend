package com.example.lms.enrollment.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.mapper.EnrollmentMapper;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.enrollment.model.EnrollmentStatus;
import com.example.lms.enrollment.repository.EnrollmentRepository;
import com.example.lms.logging.service.UserActivityLogService;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.course.service.CompletionVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // Add this import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j  // Add this annotation to create a logger
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentNotificationService enrollmentNotificationService;
    private final UserActivityLogService logService;
    private final CompletionVerificationService completionVerificationService;

    /**
     * Enroll a student in a course
     */
    @Transactional
    public EnrollmentDTO enrollStudent(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
    
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        // Log the enrollment activity
        logService.logActivity(student, "COURSE_ENROLLMENT", "Enrolled in course: " + course.getTitle());

        // Check if the course is active
        if (!course.isActive()) {
            throw new IllegalStateException("Cannot enroll in an inactive course");
        }

        // Check capacity limits
        if (course.getMaxCapacity() != null && 
            course.getStudents().size() >= course.getMaxCapacity()) {
            throw new IllegalStateException("Course capacity reached, cannot enroll more students.");
        }

        // Verify prerequisites are met
        Set<Course> prerequisites = course.getPrerequisites();
        if (prerequisites != null && !prerequisites.isEmpty()) {
            List<Long> prerequisiteCourseIds = prerequisites.stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
            
            for (Long prereqId : prerequisiteCourseIds) {
                if (!hasCompletedCourse(studentId, prereqId)) {
                    throw new IllegalStateException("Student must complete prerequisite course ID: " + prereqId);
                }
            }
        }
    
        // Check if the student is already enrolled in this course
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
            .ifPresent(e -> {
                throw new IllegalStateException("Student is already enrolled in this course.");
            });
    
        // Create new enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.APPROVED) // Directly approve for now
                .enrollmentDate(LocalDateTime.now())
                .progress(0.0)
                .lastAccessedDate(LocalDateTime.now())
                .build();
        
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
    
        // Notify the student about successful enrollment
        enrollmentNotificationService.notifyEnrollment(studentId, courseId);
    
        return EnrollmentMapper.toDTO(savedEnrollment);
    }
    
    /**
     * Check if student has completed a specific course
     */
    @Transactional(readOnly = true)
    public boolean hasCompletedCourse(Long studentId, Long courseId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .map(Enrollment::isCompleted)
                .orElse(false);
    }

    /**
     * Get all enrollments for a student
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsForStudent(Long studentId) {
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId); 
        return enrollments.stream()
                .map(EnrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all enrollments for a course
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsForCourse(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId); 
        return enrollments.stream()
                .map(EnrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update enrollment progress
     */
    @Transactional
    public EnrollmentDTO updateProgress(Long studentId, Long courseId, Double progress) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Ensure progress is within valid range (0-100%)
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }

        // Update progress
        enrollment.updateProgress(progress);
        
        // Check if course is completed based on completion requirements
        if (progress >= 100 && completionVerificationService.verifyCompletion(studentId, courseId)) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletionDate(LocalDateTime.now());
            
            // Notify completion
            enrollmentNotificationService.notifyCourseCompletion(studentId, courseId);
        }
        
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        return EnrollmentMapper.toDTO(updatedEnrollment);
    }
    
    /**
     * Complete a course enrollment
     */
    @Transactional
    public EnrollmentDTO completeCourse(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        // Verify completion requirements are met
        if (!completionVerificationService.verifyCompletion(studentId, courseId)) {
            throw new IllegalStateException("Course completion requirements not met");
        }
        
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletionDate(LocalDateTime.now());
        enrollment.setProgress(100.0); // Set to 100% when completed
        
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        
        // Notify completion
        enrollmentNotificationService.notifyCourseCompletion(studentId, courseId);
        
        return EnrollmentMapper.toDTO(updatedEnrollment);
    }
    
    /**
     * Find enrollment by student and course
     */
    @Transactional(readOnly = true)
    public EnrollmentDTO getEnrollment(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
                
        return EnrollmentMapper.toDTO(enrollment);
    }
    
    /**
     * Unenroll a student from a course
     */
    @Transactional
    public void unenrollStudent(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        // Only allow cancellation if the course hasn't been completed
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot unenroll from a completed course");
        }
        
        // Set status to cancelled
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
        
        // Log the activity
        User student = enrollment.getStudent();
        Course course = enrollment.getCourse();
        logService.logActivity(student, "COURSE_UNENROLLMENT", "Unenrolled from course: " + course.getTitle());
    }
    
    /**
     * Enroll multiple students in a course
     */
    @Transactional
    public List<EnrollmentDTO> enrollMultipleStudents(Long courseId, List<Long> studentIds) {
        List<EnrollmentDTO> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Long studentId : studentIds) {
            try {
                EnrollmentDTO enrollment = enrollStudent(studentId, courseId);
                results.add(enrollment);
            } catch (Exception e) {
                errors.add("Failed to enroll student ID " + studentId + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            // Log errors but continue with successful enrollments
            log.warn("Some enrollments failed: {}", String.join(", ", errors));
        }
        
        return results;
    }
    
    /**
     * Update enrollment status by ID
     */
    @Transactional
    public EnrollmentDTO updateEnrollmentStatus(Long enrollmentId, String status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));
    
        // Convert frontend status to enrollment status enum
        EnrollmentStatus newStatus;
        if ("active".equalsIgnoreCase(status)) {
            newStatus = EnrollmentStatus.APPROVED;
        } else if ("inactive".equalsIgnoreCase(status)) {
            newStatus = EnrollmentStatus.CANCELLED;
        } else {
            try {
                // Try to parse as enum directly if it's not active/inactive
                newStatus = EnrollmentStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid enrollment status: " + status);
            }
        }
        
        enrollment.setStatus(newStatus);
        enrollment = enrollmentRepository.save(enrollment);
        
        // Notify about status change
        notifyStatusChange(enrollment);
        
        return EnrollmentMapper.toDTO(enrollment);
    }
    
    /**
     * Notify relevant parties about enrollment status changes
     */
    private void notifyStatusChange(Enrollment enrollment) {
        // Optional: Send notifications about status changes
        try {
            // This could integrate with the notification service you already have
            if (enrollment.getStatus() == EnrollmentStatus.APPROVED) {
                enrollmentNotificationService.notifyEnrollment(
                    enrollment.getStudent().getId(), 
                    enrollment.getCourse().getId()
                );
            }
        } catch (Exception e) {
            // Log but don't fail the transaction
            log.error("Failed to send enrollment notification", e);
        }
    }
    
    /**
     * Unenroll a student by enrollment ID
     */
    @Transactional
    public void unenrollStudentById(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        // Only allow cancellation if the course hasn't been completed
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot unenroll from a completed course");
        }
        
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
        
        // Log the activity
        User student = enrollment.getStudent();
        Course course = enrollment.getCourse();
        logService.logActivity(student, "COURSE_UNENROLLMENT", "Unenrolled from course: " + course.getTitle());
    }
    
    /**
     * Update progress by enrollment ID
     */
    @Transactional
    public EnrollmentDTO updateProgressById(Long enrollmentId, Double progress, String grade) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with ID: " + enrollmentId));
        
        enrollment.updateProgress(progress);
        
        // If grade is provided, store it in custom field or attribute
        // Note: Your Enrollment entity doesn't have a grade field, you might need to add it
        
        Enrollment updated = enrollmentRepository.save(enrollment);
        return EnrollmentMapper.toDTO(updated);
    }
    
    /**
     * Count active enrollments for a specific course
     * @param courseId The course ID to count enrollments for
     * @return The number of active enrollments
     */
    @Transactional(readOnly = true)
    public long countActiveCourseEnrollments(Long courseId) {
        courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        return enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.APPROVED);
    }

    /**
     * Count all enrollments for a course regardless of status
     * @param courseId The course ID to count enrollments for
     * @return The total number of enrollments
     */
    @Transactional(readOnly = true)
    public long countAllEnrollments(Long courseId) {
        courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        return enrollmentRepository.countByCourseId(courseId);
    }
    
    /**
     * Permanently delete an enrollment 
     * 
     * @param enrollmentId ID of the enrollment to delete
     * @return true if deletion was successful
     * @throws ResourceNotFoundException if enrollment doesn't exist
     */
    @Transactional
    public boolean deleteEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));
        
        // Log activity before deletion
        try {
            // Change userActivityLogService to logService
            logService.logActivity(
                enrollment.getStudent(),  // Changed to pass the entire student object
                "COURSE_UNENROLLMENT",
                "Student unenrolled from course: " + enrollment.getCourse().getTitle()
            );
        } catch (Exception e) {
            // Log but continue with deletion
            log.error("Failed to log enrollment deletion", e);
        }
        
        // Delete the enrollment
        enrollmentRepository.delete(enrollment);
        
        // Return true to indicate successful deletion
        return true;
    }

    /**
     * Delete an enrollment by student and course IDs
     * 
     * @param studentId ID of the student
     * @param courseId ID of the course
     * @return true if deletion was successful
     * @throws ResourceNotFoundException if enrollment doesn't exist
     */
    @Transactional
    public boolean deleteEnrollmentByStudentAndCourse(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Enrollment not found for student id: " + studentId + " and course id: " + courseId));
        
        // Get student reference before deletion
        User student = enrollment.getStudent();
        String courseTitle = enrollment.getCourse().getTitle();
        
        // Delete the enrollment
        enrollmentRepository.delete(enrollment);
        
        // Log the unenrollment action
        try {
            // Change userActivityLogService to logService and adjust parameters
            logService.logActivity(
                student,
                "COURSE_UNENROLLMENT",
                "Student unenrolled from course: " + courseTitle
            );
        } catch (Exception e) {
            // Already deleted, just log the error
            log.error("Failed to log enrollment deletion", e);
        }
        
        return true;
    }
}