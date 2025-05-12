package com.example.lms.content.mapper;

import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.model.Content;
import org.springframework.stereotype.Component;

@Component
public class SimpleContentMapper {

    public ContentDTO toDTO(Content content) {
        if (content == null) return null;
        
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setDescription(content.getDescription());
        dto.setType(content.getType() != null ? content.getType().name() : null);
        dto.setOrder(content.getOrder());
        dto.setDuration(content.getDuration());
        dto.setIsPublished(content.isPublished());
        
        // Only include these if they exist and you need them
        dto.setFilePath(content.getFilePath());
        dto.setContent(content.getContent());
        dto.setVideoUrl(content.getVideoUrl());
        
        // Skip complex nested objects that might cause circular references
        if (content.getModule() != null) {
            dto.setModuleId(content.getModule().getId());
        }
        
        if (content.getCourse() != null) {
            dto.setCourseId(content.getCourse().getId());
        }
        
        return dto;
    }
}