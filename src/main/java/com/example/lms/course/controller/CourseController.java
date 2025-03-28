package com.example.lms.course.controller;

import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.dto.CourseStatisticsDTO;
import com.example.lms.course.service.CourseService;
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
    public ResponseEntity<EntityModel<CourseDTO>> createCourse(@Validated @RequestBody CourseDTO courseDTO) {
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
    public ResponseEntity<EntityModel<CourseDTO>> getCourseById(@PathVariable Long id) {
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
    public ResponseEntity<CollectionModel<EntityModel<CourseDTO>>> getCoursesByDepartment(@PathVariable Long departmentId) {
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
    public ResponseEntity<CollectionModel<EntityModel<CourseDTO>>> getCoursesByInstructor(@PathVariable Long instructorId) {
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
    public ResponseEntity<EntityModel<CourseDTO>> updateCourse(@PathVariable Long id, @Validated @RequestBody CourseDTO courseDTO) {
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
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
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
    public ResponseEntity<EntityModel<CourseDTO>> addStudentToCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
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
    public ResponseEntity<EntityModel<CourseDTO>> removeStudentFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
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
    public ResponseEntity<CourseStatisticsDTO> getCourseStatistics(@PathVariable Long id) {
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