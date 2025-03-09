package com.example.lms.course.service;

import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.mapper.CourseMapper;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.Department.model.Department;
import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // Method to delete a course
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // Method to create a new course
    public CourseDTO createCourse(CourseDTO courseDTO) {
        // Validate the fields of courseDTO
        if (courseDTO.getTitle() == null || courseDTO.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (courseDTO.getDescription() == null || courseDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (courseDTO.getInstructorEmail() == null || courseDTO.getInstructorEmail().isEmpty()) {
            throw new IllegalArgumentException("Instructor email cannot be empty");
        }

        // Find the instructor by email
        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Department department = null;
        if (courseDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(courseDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + courseDTO.getDepartmentId()));
        }
        // Convert the DTO to entity
        Course course = CourseMapper.toEntity(courseDTO, instructor, department);

        // Save the course entity to the repository
        Course savedCourse = courseRepository.save(course);

        // Return the saved course as DTO
        return CourseMapper.toDTO(savedCourse);
    }

    // Method to update a course
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        // Validate the fields of courseDTO
        if (courseDTO.getTitle() == null || courseDTO.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (courseDTO.getDescription() == null || courseDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (courseDTO.getInstructorEmail() == null || courseDTO.getInstructorEmail().isEmpty()) {
            throw new IllegalArgumentException("Instructor email cannot be empty");
        }

        // Find the existing course by id
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Find the instructor by email
        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
   
        if (courseDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(courseDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + courseDTO.getDepartmentId()));
            course.setDepartment(department);
        }
        // Update course properties
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructor(instructor);
        if (courseDTO.getMaxCapacity() != null) {
            course.setMaxCapacity(courseDTO.getMaxCapacity());
        }

        // Save the updated course entity
        Course updatedCourse = courseRepository.save(course);

        // Return the updated course as DTO
        return CourseMapper.toDTO(updatedCourse);
    }

    // Method to get a course by id
    public CourseDTO getCourseById(Long id) {
        // Find the course by id
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Return the course as DTO
        return CourseMapper.toDTO(course);
    }

    // Method to get all courses
    public List<CourseDTO> getAllCourses() {
        // Get all courses
        List<Course> courses = courseRepository.findAll();

        // Convert courses to DTOs
        return courses.stream()
                      .map(CourseMapper::toDTO)
                      .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
public List<CourseDTO> getCoursesByDepartment(Long departmentId) {
    Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
            
    List<Course> courses = courseRepository.findByDepartment(department);
    return courses.stream()
            .map(CourseMapper::toDTO)
            .collect(Collectors.toList());
}
}
