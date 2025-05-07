package com.example.lms.course.service;

import com.example.lms.course.dto.StudentCourseDTO;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.enrollment.model.EnrollmentStatus;
import com.example.lms.enrollment.repository.EnrollmentRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentDashboardService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all courses for a student, categorized by registration status
     * 
     * @param studentId ID of the student
     * @return Map of courses by type (registered, completed, available)
     */
    @Transactional(readOnly = true)
    public Map<String, List<StudentCourseDTO>> getStudentCourses(Long studentId) {
        // Verify student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        
        // Get all enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        // Map to store course lists by type
        Map<String, List<StudentCourseDTO>> coursesMap = new HashMap<>();
        coursesMap.put("registered", new ArrayList<>());
        coursesMap.put("completed", new ArrayList<>());
        coursesMap.put("available", new ArrayList<>());
        
        // Process enrolled courses
        Set<Long> enrolledCourseIds = new HashSet<>();
        
        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            enrolledCourseIds.add(course.getId());
            
            StudentCourseDTO courseDTO = mapCourseToDTO(course, enrollment);
            
            if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                coursesMap.get("completed").add(courseDTO);
            } else if (enrollment.getStatus() == EnrollmentStatus.IN_PROGRESS || 
                      enrollment.getStatus() == EnrollmentStatus.APPROVED) {
                coursesMap.get("registered").add(courseDTO);
            }
        }
        
        // Find available courses (courses student is not enrolled in)
        List<Course> allCourses = courseRepository.findAll();
        for (Course course : allCourses) {
            if (!enrolledCourseIds.contains(course.getId()) && 
                course.isPublished() && 
                !course.isArchived() && 
                course.isActive()) {
                
                StudentCourseDTO courseDTO = mapCourseToDTO(course, null);
                coursesMap.get("available").add(courseDTO);
            }
        }
        
        return coursesMap;
    }
    
    /**
     * Get current term summary for a student
     * 
     * @param studentId ID of the student
     * @return Map with term info and current courses
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentTermSummary(Long studentId) {
        // Verify student exists
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        
        // Get current term
        LocalDateTime now = LocalDateTime.now();
        String currentTerm = determineCurrentTerm(now);
        
        // Get all enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        // Filter for current term enrollments that are in progress
        List<Enrollment> currentTermEnrollments = enrollments.stream()
                .filter(e -> isCurrentTermCourse(e.getCourse(), now) && 
                       (e.getStatus() == EnrollmentStatus.IN_PROGRESS || e.getStatus() == EnrollmentStatus.APPROVED))
                .collect(Collectors.toList());
        
        // Map enrollments to StudentCourseDTO
        List<StudentCourseDTO> currentCourses = currentTermEnrollments.stream()
                .map(e -> mapCourseToDTO(e.getCourse(), e))
                .collect(Collectors.toList());
        
        // Calculate total credits
        int totalCredits = currentCourses.stream()
                .mapToInt(StudentCourseDTO::getCredits)
                .sum();
        
        // Prepare result
        Map<String, Object> result = new HashMap<>();
        result.put("term", currentTerm);
        result.put("totalCredits", totalCredits);
        result.put("courses", currentCourses);
        
        return result;
    }
    
    /**
     * Map Course and Enrollment to StudentCourseDTO
     */
    private StudentCourseDTO mapCourseToDTO(Course course, Enrollment enrollment) {
        String status = enrollment != null ? 
                (enrollment.getStatus() == EnrollmentStatus.COMPLETED ? "Completed" : "In Progress") : 
                "Available";
                
        StudentCourseDTO.CourseType type = enrollment != null ? 
                (enrollment.getStatus() == EnrollmentStatus.COMPLETED ? 
                        StudentCourseDTO.CourseType.COMPLETED : 
                        StudentCourseDTO.CourseType.REGISTERED) : 
                StudentCourseDTO.CourseType.AVAILABLE;
        
        String term = course.getSemester();
        if (term == null && course.getStartDate() != null) {
            term = determineTermFromDate(course.getStartDate());
        }
        
        String category = determineCourseCategory(course);
        
        return StudentCourseDTO.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getTitle())
                .credits(course.getCredits() != null ? course.getCredits() : 0)
                .category(category)
                .term(term)
                .status(status)
                .type(type)
                .build();
    }
    
    /**
     * Determine course category (Major Requirement, Elective, etc.)
     */
    private String determineCourseCategory(Course course) {
        // This is placeholder logic - you'll need to adapt this to your actual data model
        if (course.getTags() != null) {
            if (course.getTags().contains("major_requirement")) {
                return "Major Requirement";
            } else if (course.getTags().contains("elective")) {
                return "Major Elective";
            } else if (course.getTags().contains("medical")) {
                return "Medical Requirement";
            }
        }
        
        // Default based on department
        if (course.getDepartment() != null) {
            String deptName = course.getDepartment().getName();
            if (deptName != null) {
                if (deptName.toLowerCase().contains("medical") || deptName.toLowerCase().contains("health")) {
                    return "Medical Requirement";
                }
            }
        }
        
        return "Elective";
    }
    
    /**
     * Determine if a course is in the current term
     */
    private boolean isCurrentTermCourse(Course course, LocalDateTime now) {
        if (course.getStartDate() != null && course.getEndDate() != null) {
            return !now.isBefore(course.getStartDate()) && !now.isAfter(course.getEndDate());
        }
        
        // If we don't have dates, use the semester field if available
        if (course.getSemester() != null) {
            String currentTerm = determineCurrentTerm(now);
            return course.getSemester().equals(currentTerm);
        }
        
        return false;
    }
    
    /**
     * Determine current term based on date
     */
    private String determineCurrentTerm(LocalDateTime date) {
        int month = date.getMonthValue();
        int year = date.getYear();
        
        // Simple logic: Jan-May is Spring, Jun-Aug is Summer, Sep-Dec is Fall
        String season;
        if (month >= 1 && month <= 5) {
            season = "Spring";
        } else if (month >= 6 && month <= 8) {
            season = "Summer";
        } else {
            season = "Fall";
        }
        
        return season + "-" + year;
    }
    
    /**
     * Determine term from a date
     */
    private String determineTermFromDate(LocalDateTime date) {
        return determineCurrentTerm(date);
    }
}