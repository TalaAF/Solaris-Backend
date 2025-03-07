package com.example.lms.content.dto;

import org.springframework.hateoas.RepresentationModel;
import lombok.Data;
import com.example.lms.course.dto.CourseDTO;

@Data
public class ContentDTO extends RepresentationModel<ContentDTO> {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String fileType;
    private long fileSize;
    private String createdAt;
    private String updatedAt;
    private CourseDTO course; // The DTO will be linked to the CourseDTO.

    public ContentDTO() {}

    public ContentDTO(Long id, String title, String description, String filePath, String fileType, long fileSize, String createdAt, String updatedAt, CourseDTO course) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.course = course;
    }
}
