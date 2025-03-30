package com.example.lms.course.mapper;

import com.example.lms.Department.model.Department;
import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper class to convert between Course entities and DTOs.
 * Handles the transformations needed for API responses and database operations.
 */
public class CourseMapper {

    /**
     * Convert CourseDTO to Course entity
     * 
     * @param courseDTO DTO containing course data
     * @param instructor User entity representing the instructor
     * @param department Department entity the course belongs to
     * @return Course entity populated with data from DTO
     */
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
        
        // Set startDate and endDate
        course.setStartDate(courseDTO.getStartDate());
        course.setEndDate(courseDTO.getEndDate());
        
        // Set status flags
        course.setPublished(courseDTO.isPublished());
        course.setArchived(courseDTO.isArchived());
        
        // Set prerequisites
        if (courseDTO.getPrerequisiteCourseIds() != null && !courseDTO.getPrerequisiteCourseIds().isEmpty()) {
            Set<Course> prerequisites = new HashSet<>();
            course.setPrerequisites(prerequisites);
            
            // Note: The actual prerequisite courses need to be loaded from the repository
            // This is done in the service layer, not in the mapper
        }
    
        return course;
    }
    /**
 * Convert Course entity to CourseDTO
 * 
 * @param course Course entity to convert
 * @return CourseDTO populated with data from entity
 */
public static CourseDTO toDTO(Course course) {
    CourseDTO courseDTO = new CourseDTO();
    courseDTO.setId(course.getId());
    courseDTO.setTitle(course.getTitle());
    courseDTO.setDescription(course.getDescription());
    
    // Set instructor email if available
    if (course.getInstructor() != null) {
        courseDTO.setInstructorEmail(course.getInstructor().getEmail());
    }

    // Add department info if available
    if (course.getDepartment() != null) {
        courseDTO.setDepartmentId(course.getDepartment().getId());
        courseDTO.setDepartmentName(course.getDepartment().getName());
    }

    // Add max capacity to the DTO
    courseDTO.setMaxCapacity(course.getMaxCapacity());
    
    // Add current enrollment count
    courseDTO.setCurrentEnrollment(course.getCurrentEnrollment());

    // Set the prerequisite course IDs
    if (course.getPrerequisites() != null && !course.getPrerequisites().isEmpty()) {
        Set<Long> prerequisiteIds = course.getPrerequisites().stream()
            .map(Course::getId)
            .collect(Collectors.toSet());
        courseDTO.setPrerequisiteCourseIds(prerequisiteIds);
    }
    
    // Add startDate and endDate
    courseDTO.setStartDate(course.getStartDate());
    courseDTO.setEndDate(course.getEndDate());
    
    // Add status flags
    courseDTO.setPublished(course.isPublished());
    courseDTO.setArchived(course.isArchived());
    
    // Add metadata fields (if BaseEntity extends to Course)
    if (course.getCreatedAt() != null) {
        courseDTO.setCreatedAt(course.getCreatedAt());
    }
    
    if (course.getUpdatedAt() != null) {
        courseDTO.setUpdatedAt(course.getUpdatedAt());
    }

    return courseDTO;
}
    /**
     * Convert list of Course entities to list of CourseDTOs
     * 
     * @param courses List of Course entities
     * @return List of CourseDTOs
     */
    public static List<CourseDTO> toDTOList(List<Course> courses) {
        return courses.stream()
            .map(CourseMapper::toDTO)
            .collect(Collectors.toList());
    }
}