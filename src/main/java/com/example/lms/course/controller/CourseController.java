package com.example.lms.course.controller;

import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.service.CourseService;
import com.example.lms.course.assembler.CourseAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseAssembler courseAssembler;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
        this.courseAssembler = new CourseAssembler();
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return ResponseEntity.ok(courseAssembler.toModel(createdCourse)); // Add HATEOAS link to the DTO
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO courseDTO = courseService.getCourseById(id);
        return ResponseEntity.ok(courseAssembler.toModel(courseDTO)); // Add HATEOAS link to the DTO
    }

    @GetMapping
    public ResponseEntity<CollectionModel<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(CollectionModel.of(courses, linkTo(methodOn(CourseController.class).getAllCourses()).withSelfRel()));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<CollectionModel<CourseDTO>> getCoursesByDepartment(@PathVariable Long departmentId) {
        List<CourseDTO> courses = courseService.getCoursesByDepartment(departmentId);
        return ResponseEntity.ok(
            CollectionModel.of(
                courses, 
                linkTo(methodOn(CourseController.class).getCoursesByDepartment(departmentId)).withSelfRel()
            )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
        return ResponseEntity.ok(courseAssembler.toModel(updatedCourse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
