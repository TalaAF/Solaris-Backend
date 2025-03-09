package com.example.lms.progress.mapper;

import com.example.lms.content.model.Content;
import com.example.lms.enrollment.model.Enrollment;
import com.example.lms.progress.dto.ContentProgressDTO;
import com.example.lms.progress.model.ContentProgress;

public class ContentProgressMapper {

    // Convert ContentProgressDTO to ContentProgress entity
    public static ContentProgress toEntity(ContentProgressDTO dto, Enrollment enrollment, Content content) {
        // Create a new ContentProgress entity from the DTO
        ContentProgress contentProgress = new ContentProgress();

        // Set fields
        contentProgress.setEnrollment(enrollment); // Setting enrollment directly
        contentProgress.setContent(content); // Setting content directly
        contentProgress.setProgress(dto.getProgress());
        contentProgress.setLastUpdated(dto.getLastUpdated());

        return contentProgress;
    }

    // Convert ContentProgress entity to ContentProgressDTO
    public static ContentProgressDTO toDTO(ContentProgress contentProgress) {
        // Convert the entity to a DTO
        return ContentProgressDTO.builder()
                .id(contentProgress.getId())
                .studentId(contentProgress.getEnrollment().getStudent().getId()) // Accessing student via enrollment
                .contentId(contentProgress.getContent().getId()) // Accessing content
                .progress(contentProgress.getProgress())
                .lastUpdated(contentProgress.getLastUpdated())
                .build();
    }
}
