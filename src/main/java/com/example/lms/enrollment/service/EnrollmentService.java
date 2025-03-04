package com.example.lms.enrollment.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.enrollment.assembler.EnrollmentAssembler;
import com.example.lms.enrollment.dto.EnrollmentDTO;
import com.example.lms.enrollment.mapper.EnrollmentMapper;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.enrollment.model.EnrollmentStatus;
import com.example.lms.enrollment.repository.EnrollmentRepository;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

    public EnrollmentDTO enrollStudent(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Check if the course is already full
        if (course.getStudents().size() >= course.getMaxCapacity()) {
            throw new IllegalArgumentException("The course is already full, cannot enroll more students.");
        }

        // Check if the student is already enrolled in the course
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
}
