package com.example.lms.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration; // Changed to Integer
    private Integer order;
    private String type;          // "document", "video", "quiz"
    private String content;       // For document text or quiz JSON
    private String filePath;      // For file references
    private String videoUrl;      // For videos
    private Long moduleId;
    private Long courseId;
    private Boolean isPublished;
    
    // Add these missing fields used in convertToDTO method
    private String fileType;     // For file type information
    private Long fileSize;       // For file size information
    private String createdAt;    // Creation timestamp
    private String updatedAt;    // Last update timestamp
    private String courseName;   // Name of the course
    private String moduleName;   // Name of the module
    private List<String> tags;   // Content tags
    private Boolean published;   // Publication status
    private String fileUrl;      // URL to download the file
    private Map<String, String> author; // Author information
    private String preview;      // Content preview
}
