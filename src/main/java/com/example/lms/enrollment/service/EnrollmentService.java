package com.example.lms.enrollment.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.enrollment.assembler.EnrollmentAssembler;
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

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentAssembler enrollmentAssembler;

    @Autowired
    private EnrollmentNotificationService enrollmentNotificationService;  // Added for notifications
    @Autowired
    private UserActivityLogService logService;

    public EnrollmentDTO enrollStudent(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
    
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
logService.logActivity(student, "COURSE_ENROLLMENT", "Enrolled in course: " + course.getName());

        // Capacity check
        if (course.getStudents().size() >= course.getMaxCapacity()) {
            throw new IllegalArgumentException("Course capacity reached, cannot enroll more students.");
        }

        // Prerequisite check
        Set<Course> prerequisites = course.getPrerequisites();
        if (prerequisites != null && !prerequisites.isEmpty()) {
            List<Long> prerequisiteCourseIds = prerequisites.stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());

            List<Enrollment> studentEnrollments = enrollmentRepository.findByStudentId(studentId);
            Set<Long> completedCourses = studentEnrollments.stream()
                    .map(enrollment -> enrollment.getCourse().getId())
                    .collect(Collectors.toSet());

            // Check if the student has completed all prerequisite courses
            if (!completedCourses.containsAll(prerequisiteCourseIds)) {
                throw new IllegalArgumentException("Student must complete all prerequisite courses before enrolling.");
            }
        }
    
        // Check if the student is already enrolled in this course
        if (enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            throw new IllegalArgumentException("Student is already enrolled in this course.");
        }
    
        EnrollmentDTO enrollmentDTO = EnrollmentDTO.builder()
                .studentId(studentId)
                .courseId(courseId)
                .status(EnrollmentStatus.PENDING)
                .enrollmentDate(LocalDateTime.now())
                .build();
    
        Enrollment enrollment = enrollmentAssembler.toEntity(enrollmentDTO, student, course);
        enrollmentRepository.save(enrollment);
    
        // Notify the student about successful enrollment
        enrollmentNotificationService.notifyEnrollment(studentId, courseId);
    
        return EnrollmentMapper.toDTO(enrollment);
    }

    public List<EnrollmentDTO> getEnrollmentsForStudent(Long studentId) {
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId); 
        return enrollments.stream()
                .map(EnrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getEnrollmentsForCourse(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId); 
        return enrollments.stream()
                .map(EnrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EnrollmentDTO updateProgress(Long studentId, Long courseId, Double progress) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Ensure progress is within valid range (0-100%)
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }

        enrollment.setProgress(progress);
        enrollmentRepository.save(enrollment);

        return EnrollmentMapper.toDTO(enrollment);
    }
    /**
 * Check if a student has completed a course
 * 
 * @param studentId Student ID
 * @param courseId Course ID
 * @return true if the course is completed, false otherwise
 */
@Transactional(readOnly = true)
public boolean hasCompletedCourse(Long studentId, Long courseId) {
    Optional<Enrollment> enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
    
    // If no enrollment found, student hasn't completed the course
    if (enrollment.isEmpty()) {
        return false;
    }
    
    // Check if the enrollment status is COMPLETED
    return enrollment.get().getStatus() == EnrollmentStatus.COMPLETED;
}
}
