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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
}