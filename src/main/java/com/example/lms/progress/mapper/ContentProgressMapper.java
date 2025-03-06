package com.example.lms.progress.mapper;

import com.example.lms.progress.dto.ContentProgressDTO;
import com.example.lms.progress.model.ContentProgress;
import com.example.lms.content.model.Content;
import com.example.lms.enrollment.model.Enrollment;

public class ContentProgressMapper {

    // Convert DTO to Entity
    public static ContentProgress toEntity(ContentProgressDTO dto, Enrollment enrollment, Content content) {
        return ContentProgress.builder()
                .id(dto.getId())
                .enrollment(enrollment)
                .content(content)
                .progress(dto.getProgress())
                .lastUpdated(dto.getLastUpdated())
                .build();
    }

    // Convert Entity to DTO
    public static ContentProgressDTO toDTO(ContentProgress contentProgress) {
        return ContentProgressDTO.builder()
                .id(contentProgress.getId())
                .studentId(contentProgress.getEnrollment().getStudent().getId()) // Correct student access
                .contentId(contentProgress.getContent().getId())
                .progress(contentProgress.getProgress())
                .lastUpdated(contentProgress.getLastUpdated())
                .build();
    }
}
