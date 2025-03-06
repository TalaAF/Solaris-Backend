package com.example.lms.course.assembler;
import com.example.lms.course.dto.CourseDTO;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import com.example.lms.course.controller.CourseController;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class CourseAssembler extends RepresentationModelAssemblerSupport<CourseDTO, CourseDTO> {

    public CourseAssembler() {
        super(CourseController.class, CourseDTO.class);
    }

    @Override
    public CourseDTO toModel(CourseDTO courseDTO) {
        // Add HATEOAS link to the courseDTO
        courseDTO.add(linkTo(methodOn(CourseController.class).getCourseById(courseDTO.getId())).withSelfRel());
        
        // Add additional links for update and delete
        courseDTO.add(linkTo(methodOn(CourseController.class).updateCourse(courseDTO.getId(), courseDTO)).withRel("update"));
        courseDTO.add(linkTo(methodOn(CourseController.class).deleteCourse(courseDTO.getId())).withRel("delete"));
        
        return courseDTO;
    }
}
