package com.example.lms.course.dto;

import java.util.List;
import java.util.Set;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO extends RepresentationModel<CourseDTO> {
    private Long id;
    private String title;
    private String description;
    private String instructorEmail; 
    private Long departmentId;
    private String departmentName;
    private Integer maxCapacity; 
    private List<Long> prerequisiteCourseIds; 

    public CourseDTO(Long id, String title, String description, String instructorEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructorEmail = instructorEmail; 
    }
}
