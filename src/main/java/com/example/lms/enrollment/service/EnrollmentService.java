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
        // Check if the student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Check if the course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Check if student is already enrolled in the course
        if (enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            throw new IllegalArgumentException("Student is already enrolled in this course.");
        }

        // Check if the course has reached its maximum capacity
        if (enrollmentRepository.findByCourseId(courseId).size() >= course.getMaxCapacity()) {
            throw new IllegalArgumentException("Course is full.");
        }

        // Check if the student has completed the prerequisite courses
        List<Course> prerequisites = (List<Course>) course.getPrerequisites();
        for (Course prerequisite : prerequisites) {
            boolean completedPrerequisite = enrollmentRepository.findByStudentIdAndCourseId(studentId, prerequisite.getId())
                    .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                    .isPresent();
            if (!completedPrerequisite) {
                throw new IllegalArgumentException("Student has not completed the prerequisite course: " + prerequisite.getName());
            }
        }

        // Create the enrollment DTO
        EnrollmentDTO enrollmentDTO = EnrollmentDTO.builder()
                .studentId(studentId)
                .courseId(courseId)
                .status(EnrollmentStatus.PENDING)
                .enrollmentDate(LocalDateTime.now())
                .build();

        // Convert DTO to entity and save the enrollment
        Enrollment enrollment = enrollmentAssembler.toEntity(enrollmentDTO, student, course);
        enrollmentRepository.save(enrollment);

        // Return the enrollment DTO
        return EnrollmentMapper.toDTO(enrollment);
    }

    public List<EnrollmentDTO> getEnrollmentsForStudent(Long studentId) {
        // Check if the student exists
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Fetch the list of enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .map(EnrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getEnrollmentsForCourse(Long courseId) {
        // Check if the course exists
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Fetch the list of enrollments for the course
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return enrollments.stream()
                .map(EnrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }
}
