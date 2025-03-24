package com.example.lms.content.dto;

import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.lms.course.dto.CourseDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO extends RepresentationModel<ContentDTO> {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String fileType;
    private long fileSize;
    private String createdAt;
    private String updatedAt;
    private Long courseId;
    private String courseName;
    private Long moduleId;
    private String moduleName;
    private Integer orderInModule;
    private List<String> tags;
    private String preview;

   
}
