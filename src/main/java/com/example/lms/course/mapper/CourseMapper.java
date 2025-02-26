package com.example.lms.course.mapper;

import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;

public class CourseMapper {

    // Convert CourseDTO to Course entity
    public static Course toEntity(CourseDTO courseDTO, User instructor) {
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructor(instructor);
        return course;
    }

    // Convert Course entity to CourseDTO
    public static CourseDTO toDTO(Course course) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(course.getId());
        courseDTO.setTitle(course.getTitle());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setInstructorEmail(course.getInstructor().getEmail()); // Assuming Course has Instructor and Instructor has email
        return courseDTO;
    }
}
