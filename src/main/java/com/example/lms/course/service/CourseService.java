package com.example.lms.course.service;

import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.dto.CourseStatisticsDTO;
import com.example.lms.course.mapper.CourseMapper;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import com.example.lms.Department.model.Department;
import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.logging.service.UserActivityLogService;
import com.example.lms.enrollment.service.EnrollmentService;
import com.example.lms.content.service.ContentService;
import com.example.lms.progress.service.ProgressService;
import com.example.lms.assessment.repository.QuizRepository;
import java.util.Set;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserActivityLogService logService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private ContentService contentService;
    
    @Autowired
    private ProgressService progressService;
    
    @Autowired
    private QuizRepository quizRepository;

    /**
     * Get all courses
     * 
     * @return List of course DTOs
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "courses")
    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                  .map(CourseMapper::toDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Get course by ID
     * 
     * @param id Course ID
     * @return Course DTO
     * @throws ResourceNotFoundException if course not found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "#id")
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return CourseMapper.toDTO(course);
    }

    /**
     * Create a new course
     * 
     * @param courseDTO Course data
     * @return Created course DTO
     * @throws ResourceNotFoundException if instructor or department not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseDTO createCourse(CourseDTO courseDTO) {
        // Validate the fields of courseDTO
        validateCourseDTO(courseDTO);

        // Find the instructor by email
        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with email: " + courseDTO.getInstructorEmail()));

        Department department = null;
        if (courseDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(courseDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + courseDTO.getDepartmentId()));
        }
        
        // Convert the DTO to entity
        Course course = CourseMapper.toEntity(courseDTO, instructor, department);

        // Process prerequisites if provided
        if (courseDTO.getPrerequisiteCourseIds() != null && !courseDTO.getPrerequisiteCourseIds().isEmpty()) {
            Set<Course> prerequisites = new HashSet<>();
            for (Long prerequisiteId : courseDTO.getPrerequisiteCourseIds()) {
                Course prerequisite = courseRepository.findById(prerequisiteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Prerequisite course not found with id: " + prerequisiteId));
                prerequisites.add(prerequisite);
            }
            course.setPrerequisites(prerequisites);
        }

        // Set additional course properties that might be missing in the mapper
        if (courseDTO.getCredits() != null) {
            course.setCredits(courseDTO.getCredits());
        }
        
        // Handle semester field (ensuring we capture it from either field name)
        String semester = courseDTO.getSemester() != null ? courseDTO.getSemester() : courseDTO.getSemesterName();
        if (semester != null) {
            course.setSemester(semester);
        }
        
        // Save the course entity to the repository
        Course savedCourse = courseRepository.save(course);
        
        // Log the activity
        logService.logActivity(instructor, "COURSE_CREATED", "Created course: " + course.getTitle());

        // Return the saved course as DTO
        return CourseMapper.toDTO(savedCourse);
    }

    /**
     * Update an existing course
     * 
     * @param id Course ID
     * @param courseDTO Updated course data
     * @return Updated course DTO
     * @throws ResourceNotFoundException if course, instructor, or department not found
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        // Validate the fields of courseDTO
        validateCourseDTO(courseDTO);

        // Find the existing course by id
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Find the instructor by email
        User instructor = userRepository.findByEmail(courseDTO.getInstructorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with email: " + courseDTO.getInstructorEmail()));
   
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
        
        // Update published status - handle both field naming conventions
        Boolean publishedStatus = null;
        if (courseDTO.getPublished() != course.isPublished()) {
            publishedStatus = courseDTO.getPublished();
        } else if (courseDTO.getPublished() != course.isPublished()) {
            publishedStatus = courseDTO.getPublished();
        }
        
        if (publishedStatus != null) {
            course.setPublished(publishedStatus);
        }
        
        // Update semester field - handle both field naming conventions
        String semester = courseDTO.getSemester() != null ? courseDTO.getSemester() : courseDTO.getSemesterName();
        if (semester != null) {
            course.setSemester(semester);
        }
        
        // Update credits
        if (courseDTO.getCredits() != null) {
            course.setCredits(courseDTO.getCredits());
        }
        
        // Update code if provided
        if (courseDTO.getCode() != null) {
            course.setCode(courseDTO.getCode());
        }
        
        // Set prerequisites if provided
        if (courseDTO.getPrerequisiteCourseIds() != null && !courseDTO.getPrerequisiteCourseIds().isEmpty()) {
            course.getPrerequisites().clear();
            for (Long prerequisiteId : courseDTO.getPrerequisiteCourseIds()) {
                Course prerequisiteCourse = courseRepository.findById(prerequisiteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Prerequisite course not found with id: " + prerequisiteId));
                course.getPrerequisites().add(prerequisiteCourse);
            }
        }

        // Save the updated course entity
        Course updatedCourse = courseRepository.save(course);
        
        // Log the activity
        logService.logActivity(instructor, "COURSE_UPDATED", "Updated course: " + course.getTitle());

        // Return the updated course as DTO
        return CourseMapper.toDTO(updatedCourse);
    }

    /**
     * Soft delete a course by marking it as archived and unpublished
     *
     * @param id Course ID
     * @throws ResourceNotFoundException if course not found
     */
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Instead of checking for constraints and throwing exceptions,
        // simply mark the course as archived and unpublished
        course.setArchived(true);
        course.setPublished(false);

        // Log the activity
        User admin = course.getInstructor(); // Using instructor for logging as admin is not readily available
        logService.logActivity(admin, "COURSE_ARCHIVED", "Archived course: " + course.getTitle());

        // Save the updated course
        courseRepository.save(course);
    }

    /**
     * Get courses by department
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "coursesByDepartment", key = "#departmentId")
    public List<CourseDTO> getCoursesByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
                
        List<Course> courses = courseRepository.findByDepartment(department);
        return courses.stream()
                .map(CourseMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get courses by instructor
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + instructorId));
                
        List<Course> courses = courseRepository.findByInstructor(instructor);
        return courses.stream()
                .map(CourseMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Add a student to a course
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public CourseDTO addStudentToCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
                
        // Check capacity
        if (course.getMaxCapacity() != null && course.getStudents().size() >= course.getMaxCapacity()) {
            throw new IllegalStateException("Course has reached maximum capacity");
        }
        
        // Check prerequisites
        if (course.getPrerequisites() != null && !course.getPrerequisites().isEmpty()) {
            for (Course prerequisite : course.getPrerequisites()) {
                boolean hasCompleted = enrollmentService.hasCompletedCourse(studentId, prerequisite.getId());
                if (!hasCompleted) {
                    throw new IllegalStateException("Student must complete prerequisite course: " + prerequisite.getTitle());
                }
            }
        }
        
        // Add student to course if not already enrolled
        if (!course.getStudents().contains(student)) {
            course.getStudents().add(student);
            courseRepository.save(course);
            
            // Create enrollment record
            enrollmentService.enrollStudent(studentId, courseId);
            
            // Log the activity
            logService.logActivity(student, "COURSE_ENROLLMENT", "Enrolled in course: " + course.getTitle());
        }
        
        return CourseMapper.toDTO(course);
    }
    
    /**
     * Remove a student from a course
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public CourseDTO removeStudentFromCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
                
        // Remove student from course if enrolled
        if (course.getStudents().contains(student)) {
            course.getStudents().remove(student);
            courseRepository.save(course);
            
            // Log the activity
            logService.logActivity(student, "COURSE_UNENROLLMENT", "Unenrolled from course: " + course.getTitle());
        }
        
        return CourseMapper.toDTO(course);
    }
    
    /**
     * Get course statistics
     */
    @Transactional(readOnly = true)
    public CourseStatisticsDTO getCourseStatistics(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                
        CourseStatisticsDTO stats = new CourseStatisticsDTO();
        stats.setCourseId(courseId);
        stats.setCourseName(course.getTitle());
        stats.setTotalStudents(course.getStudents().size());
        stats.setTotalContent(course.getContents().size());
        stats.setTotalQuizzes(course.getQuizzes().size());
        
        // Calculate average completion percentage
        if (!course.getStudents().isEmpty()) {
            double totalProgress = 0.0;
            for (User student : course.getStudents()) {
                Double progress = progressService.getProgressPercentage(student.getId(), courseId);
                totalProgress += progress != null ? progress : 0.0;
            }
            stats.setAverageCompletionPercentage(totalProgress / course.getStudents().size());
        }
        
        return stats;
    }
    
    /**
     * Get course with user-specific progress
     */
    @Transactional(readOnly = true)
    public CourseDTO getCourseWithProgress(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
                
        CourseDTO courseDTO = CourseMapper.toDTO(course);
        
        // Get user's progress if userId provided
        if (userId != null) {
            Double progress = progressService.getProgressPercentage(userId, courseId);
            if (progress != null) {
                courseDTO.setProgress(progress.intValue());
            }
        }
        
        return courseDTO;
    }

    /**
     * Get all courses with progress for a specific user
     */
    @Transactional(readOnly = true) 
    public List<CourseDTO> getCoursesWithProgress(Long userId) {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(course -> {
                    CourseDTO dto = CourseMapper.toDTO(course);
                    
                    // Get progress for this specific user and course
                    Double progress = progressService.getProgressPercentage(userId, course.getId());
                    if (progress != null) {
                        dto.setProgress(progress.intValue());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get courses with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<CourseDTO> getCourses(int page, int size, String sortBy, String sortDirection, Map<String, Object> filters) {
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by title or search term
            if (filters.containsKey("title")) {
                String title = (String) filters.get("title");
                predicates.add(cb.like(cb.lower(root.get("title")), 
                    "%" + title.toLowerCase() + "%"));
            }
            
            if (filters.containsKey("search")) {
                String search = (String) filters.get("search");
                predicates.add(
                    cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
                    )
                );
            }
            
            // Filter by department
            if (filters.containsKey("departmentId")) {
                Object departmentId = filters.get("departmentId");
                Long deptId;
                if (departmentId instanceof String) {
                    deptId = Long.parseLong((String) departmentId);
                } else {
                    deptId = (Long) departmentId;
                }
                predicates.add(cb.equal(root.get("department").get("id"), deptId));
            }
            
            // Filter by instructor
            if (filters.containsKey("instructorId")) {
                Object instructorId = filters.get("instructorId");
                Long instId;
                if (instructorId instanceof String) {
                    instId = Long.parseLong((String) instructorId);
                } else {
                    instId = (Long) instructorId;
                }
                predicates.add(cb.equal(root.get("instructor").get("id"), instId));
            }
            
            // Filter by instructor email
            if (filters.containsKey("instructorEmail")) {
                String instructorEmail = (String) filters.get("instructorEmail");
                predicates.add(cb.equal(root.get("instructor").get("email"), instructorEmail));
            }
            
            // Filter by published status - handle both field naming conventions
            if (filters.containsKey("published")) {
                Boolean published = parseBoolean(filters.get("published"));
                predicates.add(cb.equal(root.get("published"), published));
            } else if (filters.containsKey("isPublished")) {
                Boolean isPublished = parseBoolean(filters.get("isPublished"));
                predicates.add(cb.equal(root.get("published"), isPublished));
            }
            
            // Filter by archived status
            if (filters.containsKey("archived")) {
                Boolean archived = parseBoolean(filters.get("archived"));
                predicates.add(cb.equal(root.get("archived"), archived));
            } else if (filters.containsKey("isArchived")) {
                Boolean isArchived = parseBoolean(filters.get("isArchived"));
                predicates.add(cb.equal(root.get("archived"), isArchived));
            }
            
            // Filter by semester
            if (filters.containsKey("semester")) {
                String semester = (String) filters.get("semester");
                predicates.add(cb.equal(root.get("semester"), semester));
            } else if (filters.containsKey("semesterName")) {
                String semesterName = (String) filters.get("semesterName");
                predicates.add(cb.equal(root.get("semester"), semesterName));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return coursePage.map(CourseMapper::toDTO);
    }
    
    // Helper method to parse boolean values from filters
    private Boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
    
    /**
     * Validate course DTO fields
     * 
     * @param courseDTO Course DTO to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCourseDTO(CourseDTO courseDTO) {
        if (courseDTO.getTitle() == null || courseDTO.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (courseDTO.getDescription() == null || courseDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (courseDTO.getInstructorEmail() == null || courseDTO.getInstructorEmail().isEmpty()) {
            throw new IllegalArgumentException("Instructor email cannot be empty");
        }
        if (courseDTO.getMaxCapacity() != null && courseDTO.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("Maximum capacity must be a positive number");
        }
    }
}