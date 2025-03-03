package com.example.lms.course.dto;

import org.springframework.hateoas.RepresentationModel;

import lombok.Data;
@Data
public class CourseDTO extends RepresentationModel<CourseDTO> {
    private Long id;
    private String title;
    private String description;
    private String instructorEmail; 
    private Long departmentId;
    private String departmentName;

    public CourseDTO() {}

    public CourseDTO(Long id, String title, String description, String instructorEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructorEmail = instructorEmail; 
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructorEmail() { return instructorEmail; } 
    public void setInstructorEmail(String instructorEmail) { this.instructorEmail = instructorEmail; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
