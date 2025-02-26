package com.example.lms.course.service;

import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.mapper.CourseMapper;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

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

        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = CourseMapper.toEntity(courseDTO, instructor);
        Course savedCourse = courseRepository.save(course);
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

        // Find the course by id
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Find the instructor
        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        // Update the course details
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructor(instructor);

        // Save the updated course
        Course updatedCourse = courseRepository.save(course);
        return CourseMapper.toDTO(updatedCourse);
    }

    // Method to get a course by id
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return CourseMapper.toDTO(course);  // Assuming you have a mapper to convert Course to CourseDTO
    }

    // Method to get all courses
    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                      .map(CourseMapper::toDTO)  // Assuming you have a mapper to convert Course to CourseDTO
                      .collect(Collectors.toList());
    }
}
