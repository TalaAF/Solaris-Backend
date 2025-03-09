package com.example.lms.course.mapper;

import com.example.lms.Department.model.Department;
import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CourseMapper {

    // Convert CourseDTO to Course entity
    public static Course toEntity(CourseDTO courseDTO, User instructor, Department department) {
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructor(instructor);
        
        // Set the department if provided
        if (department != null) {
            course.setDepartment(department);
        }
        
        // Set maxCapacity from DTO to entity
        if (courseDTO.getMaxCapacity() != null) {
            course.setMaxCapacity(courseDTO.getMaxCapacity());
        }
        
        // Set prerequisites (mapping from courseDTO.prerequisiteCourseIds to the Course's prerequisites)
        Set<Course> prerequisites = new HashSet<>();
        if (courseDTO.getPrerequisiteCourseIds() != null) {
            for (Long prerequisiteId : courseDTO.getPrerequisiteCourseIds()) {
                Course prerequisiteCourse = new Course(); // Fetch the prerequisite course by ID from the database
                prerequisiteCourse.setId(prerequisiteId);
                prerequisites.add(prerequisiteCourse);
            }
            course.setPrerequisites(prerequisites);
        }

        return course;
    }

    // Convert Course entity to CourseDTO
    public static CourseDTO toDTO(Course course) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(course.getId());
        courseDTO.setTitle(course.getTitle());
        courseDTO.setDescription(course.getDescription());
        courseDTO.setInstructorEmail(course.getInstructor().getEmail()); // Assuming Course has Instructor and Instructor has email

        if (course.getDepartment() != null) {
            courseDTO.setDepartmentId(course.getDepartment().getId());
            courseDTO.setDepartmentName(course.getDepartment().getName());
        }

        // Add max capacity to the DTO
        courseDTO.setMaxCapacity(course.getMaxCapacity());

        // Set the prerequisite course IDs
        if (course.getPrerequisites() != null) {
            Set<Long> prerequisiteIds = new HashSet<>();
            for (Course prerequisite : course.getPrerequisites()) {
                prerequisiteIds.add(prerequisite.getId());
            }
            courseDTO.setPrerequisiteCourseIds((List<Long>) prerequisiteIds);
        }

        return courseDTO;
    }
}
