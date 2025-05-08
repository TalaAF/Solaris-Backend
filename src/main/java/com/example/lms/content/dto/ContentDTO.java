package com.example.lms.content.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String createdAt;
    private String updatedAt;
    private Long courseId;
    private String courseName;
    private Long moduleId;
    private String moduleName;
    private Integer order;
    private List<String> tags;
    private String preview;
    private boolean published; // Changed from isPublished to published
    private Integer duration;
    private String fileUrl;
    private Map<String, String> author;

    // Add this constructor to match the parameters used in the first convertToDTO method
    public ContentDTO(
        Long id,
        String title, 
        String description, 
        String filePath, 
        String fileType, 
        Long fileSize, 
        String createdAt, 
        String updatedAt, 
        Long courseId, 
        String courseName, 
        Long moduleId, 
        String moduleName, 
        Integer order, 
        List<String> tags, 
        String preview) {
        
        this.id = id;
        this.title = title;
        this.description = description;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.courseId = courseId;
        this.courseName = courseName;
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.order = order;
        this.tags = tags;
        this.preview = preview;
    }
}
