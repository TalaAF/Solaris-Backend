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
        
        // Copy basic fields
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructor(instructor);
        course.setCode(courseDTO.getCode());
        
        // Set the department if provided
        if (department != null) {
            course.setDepartment(department);
        }
        
        // Set maxCapacity from DTO to entity
        if (courseDTO.getMaxCapacity() != null) {
            course.setMaxCapacity(courseDTO.getMaxCapacity());
        }
        
        // Set startDate and endDate with null checks
        if (courseDTO.getStartDate() != null) {
            course.setStartDate(courseDTO.getStartDate());
        }
        
        if (courseDTO.getEndDate() != null) {
            course.setEndDate(courseDTO.getEndDate());
        }
        
        // Handle published status from either isPublished or published field
        course.setPublished(courseDTO.getPublished());
        
        // Handle archived status
        course.setArchived(courseDTO.getArchived());
        
        // Handle semester with fallbacks for different field names
        String semester = null;
        if (courseDTO.getSemesterName() != null) {
            semester = courseDTO.getSemesterName();
        } else if (courseDTO.getSemester() != null) {
            semester = courseDTO.getSemester();
        }
        
        if (semester != null) {
            course.setSemester(semester);
        }
        
        // Handle credits if present in the Course entity
        if (courseDTO.getCredits() != null) {
            course.setCredits(courseDTO.getCredits());
        }
        
        // Set prerequisites
        if (courseDTO.getPrerequisiteCourseIds() != null && !courseDTO.getPrerequisiteCourseIds().isEmpty()) {
            Set<Course> prerequisites = new HashSet<>();
            course.setPrerequisites(prerequisites);
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
        
        // Add code field
        courseDTO.setCode(course.getCode());
        
        // Set instructor information
        if (course.getInstructor() != null) {
            courseDTO.setInstructorEmail(course.getInstructor().getEmail());
            courseDTO.setInstructorId(course.getInstructor().getId());
            courseDTO.setInstructorName(course.getInstructor().getFullName());
        }

        // Add department info if available
        if (course.getDepartment() != null) {
            courseDTO.setDepartmentId(course.getDepartment().getId());
            courseDTO.setDepartmentName(course.getDepartment().getName());
        }

        // Add max capacity and enrollment count
        courseDTO.setMaxCapacity(course.getMaxCapacity());
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
        
        // Add status flags - make sure both isPublished/published are set
        courseDTO.setPublished(course.isPublished());
        courseDTO.setArchived(course.isArchived());
        
        // Set the semester field (both semester and semesterName)
        if (course.getSemester() != null) {
            courseDTO.setSemester(course.getSemester());
            courseDTO.setSemesterName(course.getSemester());
        }
        
        // Set credits
        courseDTO.setCredits(course.getCredits());
        
        // Add metadata fields
        if (course.getCreatedAt() != null) {
            courseDTO.setCreatedAt(course.getCreatedAt());
        }
        
        if (course.getUpdatedAt() != null) {
            courseDTO.setUpdatedAt(course.getUpdatedAt());
        }
        
        // Set content and quiz counts
        courseDTO.setContentCount(course.getContents() != null ? course.getContents().size() : 0);
        courseDTO.setQuizCount(course.getQuizzes() != null ? course.getQuizzes().size() : 0);

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