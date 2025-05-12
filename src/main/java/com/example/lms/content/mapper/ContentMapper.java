package com.example.lms.content.mapper;

import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.model.Content;
import com.example.lms.content.model.ContentType;

import java.util.List;
import java.util.stream.Collectors;

public class ContentMapper {

    public static ContentDTO toDTO(Content content) {
        return ContentDTO.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .duration(content.getDuration())
                .order(content.getOrder())
                .type(content.getType() != null ? content.getType().name() : null)
                .content(content.getContent())
                .filePath(content.getFilePath())
                .videoUrl(content.getVideoUrl())
                .moduleId(content.getModule() != null ? content.getModule().getId() : null)
                .courseId(content.getCourse() != null ? content.getCourse().getId() : null)
                .isPublished(content.isPublished())
                .build();
    }
    
    public static List<ContentDTO> toDTOList(List<Content> contents) {
        return contents.stream()
                .map(ContentMapper::toDTO)
                .collect(Collectors.toList());
    }
}