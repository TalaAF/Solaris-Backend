package com.example.lms.course.service;

import com.example.lms.course.dto.CourseDTO;
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

    // Create a new course
    public CourseDTO createCourse(CourseDTO courseDTO) {
       
        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        // Create and set course details
        Course course = new Course();
        course.setName(courseDTO.getTitle()); // Set course title
        course.setDescription(courseDTO.getDescription()); // Set course description
        course.setInstructor(instructor); // Set instructor

        // Save the course and return the CourseDTO
        Course savedCourse = courseRepository.save(course);
        return new CourseDTO(savedCourse.getId(), savedCourse.getName(), savedCourse.getDescription(), instructor.getEmail());
    }

    // Get a course by its ID
    public CourseDTO getCourseById(Long id) {
        // Find the course by ID
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Return the course details as a DTO
        return new CourseDTO(course.getId(), course.getName(), course.getDescription(), course.getInstructor().getEmail());
    }

    // Get all courses
    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();

        // Convert the list of courses to CourseDTOs
        return courses.stream()
                .map(course -> new CourseDTO(course.getId(), course.getName(), course.getDescription(), course.getInstructor().getEmail()))
                .collect(Collectors.toList());
    }

    // Update an existing course
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        // Find the course by ID
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        // Update the course details
        course.setName(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructor(instructor);

        // Save the updated course and return the CourseDTO
        Course updatedCourse = courseRepository.save(course);
        return new CourseDTO(updatedCourse.getId(), updatedCourse.getName(), updatedCourse.getDescription(), instructor.getEmail());
    }

    // Delete a course by its ID
    public void deleteCourse(Long id) {
        // Find the course by ID
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Delete the course from the repository
        courseRepository.delete(course);
    }
}
