package com.example.lms.course.controller;

import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.dto.CourseStatisticsDTO;
import com.example.lms.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST API controller for course management
 */
@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course Management", description = "APIs for managing courses in the LMS")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Create a new course
     * 
     * @param courseDTO Course data
     * @return Created course with HATEOAS links
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Create a new course", 
        description = "Creates a new course with the provided details. Requires ADMIN or INSTRUCTOR role.",
        tags = {"Course Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Course created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid course data provided"),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Instructor or department not found")
    })

    public ResponseEntity<EntityModel<CourseDTO>> createCourse(
            @Parameter(description = "Course details", required = true)
            @Validated @RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        EntityModel<CourseDTO> resource = EntityModel.of(createdCourse);
        addLinks(resource, createdCourse.getId());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * Get a course by ID
     * 
     * @param id Course ID
     * @return Course with HATEOAS links
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get course by ID", 
        description = "Retrieves detailed information about a specific course by its ID",
        tags = {"Course Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Course found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    public ResponseEntity<EntityModel<CourseDTO>> getCourseById(
            @Parameter(description = "Course ID", required = true)
            @PathVariable Long id) {
        CourseDTO courseDTO = courseService.getCourseById(id);
        EntityModel<CourseDTO> resource = EntityModel.of(courseDTO);
        addLinks(resource, id);
        return ResponseEntity.ok(resource);
    }

    /**
     * Get all courses
     * 
     * @return Collection of courses with HATEOAS links
     */
    @GetMapping
    @Operation(
        summary = "Get all courses", 
        description = "Retrieves a list of all courses in the system",
        tags = {"Course Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Courses retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        )
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    public ResponseEntity<CollectionModel<EntityModel<CourseDTO>>> getAllCourses() {
        List<EntityModel<CourseDTO>> courses = courseService.getAllCourses().stream()
                .map(courseDTO -> {
                    EntityModel<CourseDTO> resource = EntityModel.of(courseDTO);
                    addLinks(resource, courseDTO.getId());
                    return resource;
                })
                .collect(Collectors.toList());
                
        CollectionModel<EntityModel<CourseDTO>> resources = CollectionModel.of(
            courses,
            linkTo(methodOn(CourseController.class).getAllCourses()).withSelfRel()
        );
        
        return ResponseEntity.ok(resources);
    }

    /**
     * Get courses by department
     * 
     * @param departmentId Department ID
     * @return Collection of courses with HATEOAS links
     */
    @GetMapping("/department/{departmentId}")
    @Operation(
        summary = "Get courses by department", 
        description = "Retrieves all courses belonging to a specific department",
        tags = {"Course Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Courses retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    public ResponseEntity<CollectionModel<EntityModel<CourseDTO>>> getCoursesByDepartment(
            @Parameter(description = "Department ID", required = true)
            @PathVariable Long departmentId) {
        List<EntityModel<CourseDTO>> courses = courseService.getCoursesByDepartment(departmentId).stream()
                .map(courseDTO -> {
                    EntityModel<CourseDTO> resource = EntityModel.of(courseDTO);
                    addLinks(resource, courseDTO.getId());
                    return resource;
                })
                .collect(Collectors.toList());
                
        CollectionModel<EntityModel<CourseDTO>> resources = CollectionModel.of(
            courses,
            linkTo(methodOn(CourseController.class).getCoursesByDepartment(departmentId)).withSelfRel()
        );
        
        return ResponseEntity.ok(resources);
    }
    
    /**
     * Get courses by instructor
     * 
     * @param instructorId Instructor ID
     * @return Collection of courses with HATEOAS links
     */
    @GetMapping("/instructor/{instructorId}")
    @Operation(
        summary = "Get courses by instructor", 
        description = "Retrieves all courses taught by a specific instructor",
        tags = {"Course Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Courses retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Instructor not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or hasRole('STUDENT')")
    public ResponseEntity<CollectionModel<EntityModel<CourseDTO>>> getCoursesByInstructor(
            @Parameter(description = "Instructor ID", required = true)
            @PathVariable Long instructorId) {
        List<EntityModel<CourseDTO>> courses = courseService.getCoursesByInstructor(instructorId).stream()
                .map(courseDTO -> {
                    EntityModel<CourseDTO> resource = EntityModel.of(courseDTO);
                    addLinks(resource, courseDTO.getId());
                    return resource;
                })
                .collect(Collectors.toList());
                
        CollectionModel<EntityModel<CourseDTO>> resources = CollectionModel.of(
            courses,
            linkTo(methodOn(CourseController.class).getCoursesByInstructor(instructorId)).withSelfRel()
        );
        
        return ResponseEntity.ok(resources);
    }

    /**
     * Update an existing course
     * 
     * @param id Course ID
     * @param courseDTO Updated course data
     * @return Updated course with HATEOAS links
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Update a course", 
        description = "Updates an existing course with the provided details. Requires ADMIN or INSTRUCTOR role.",
        tags = {"Course Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Course updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid course data provided"),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course, instructor, or department not found")
    })
    public ResponseEntity<EntityModel<CourseDTO>> updateCourse(
            @Parameter(description = "Course ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated course details", required = true) 
            @Validated @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
        EntityModel<CourseDTO> resource = EntityModel.of(updatedCourse);
        addLinks(resource, id);
        return ResponseEntity.ok(resource);
    }

    /**
     * Delete a course
     * 
     * @param id Course ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete a course", 
        description = "Deletes a course by its ID. Requires ADMIN role. Cannot delete courses with active enrollments, content, or quizzes.",
        tags = {"Course Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete course with active enrollments, content, or quizzes")
    })
    public ResponseEntity<Void> deleteCourse(
            @Parameter(description = "Course ID", required = true)
            @PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Add a student to a course
     * 
     * @param courseId Course ID
     * @param studentId Student ID
     * @return Updated course with HATEOAS links
     */
    @PostMapping("/{courseId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Enroll student in course", 
        description = "Enrolls a student in a specific course. Requires ADMIN or INSTRUCTOR role.",
        tags = {"Course Management", "Enrollment"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Student enrolled successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course or student not found"),
        @ApiResponse(responseCode = "409", description = "Course has reached maximum capacity or student doesn't meet prerequisites")
    })
    public ResponseEntity<EntityModel<CourseDTO>> addStudentToCourse(
            @Parameter(description = "Course ID", required = true) @PathVariable Long courseId,
            @Parameter(description = "Student ID", required = true) @PathVariable Long studentId) {
        CourseDTO updatedCourse = courseService.addStudentToCourse(courseId, studentId);
        EntityModel<CourseDTO> resource = EntityModel.of(updatedCourse);
        addLinks(resource, courseId);
        return ResponseEntity.ok(resource);
    }
    
    /**
     * Remove a student from a course
     * 
     * @param courseId Course ID
     * @param studentId Student ID
     * @return Updated course with HATEOAS links
     */
    @DeleteMapping("/{courseId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Unenroll student from course", 
        description = "Removes a student from a specific course. Requires ADMIN or INSTRUCTOR role.",
        tags = {"Course Management", "Enrollment"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Student unenrolled successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course or student not found")
    })
    public ResponseEntity<EntityModel<CourseDTO>> removeStudentFromCourse(
            @Parameter(description = "Course ID", required = true) @PathVariable Long courseId,
            @Parameter(description = "Student ID", required = true) @PathVariable Long studentId) {
        CourseDTO updatedCourse = courseService.removeStudentFromCourse(courseId, studentId);
        EntityModel<CourseDTO> resource = EntityModel.of(updatedCourse);
        addLinks(resource, courseId);
        return ResponseEntity.ok(resource);
    }
    
    /**
     * Get course statistics
     * 
     * @param id Course ID
     * @return Course statistics
     */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @Operation(
        summary = "Get course statistics", 
        description = "Retrieves statistical information about a specific course. Requires ADMIN or INSTRUCTOR role.",
        tags = {"Course Management", "Analytics"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseStatisticsDTO.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden, insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseStatisticsDTO> getCourseStatistics(
            @Parameter(description = "Course ID", required = true)
            @PathVariable Long id) {
        CourseStatisticsDTO statistics = courseService.getCourseStatistics(id);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Helper method to add HATEOAS links to a course resource
     * 
     * @param resource EntityModel to add links to
     * @param id Course ID
     */
    private void addLinks(EntityModel<CourseDTO> resource, Long id) {
        // Self link
        resource.add(linkTo(methodOn(CourseController.class).getCourseById(id)).withSelfRel());
        
        // Related links
        resource.add(linkTo(methodOn(CourseController.class).getCourseStatistics(id)).withRel("statistics"));
        resource.add(linkTo(methodOn(CourseController.class).updateCourse(id, null)).withRel("update"));
        resource.add(linkTo(methodOn(CourseController.class).deleteCourse(id)).withRel("delete"));
    }
}