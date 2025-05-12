package com.example.lms.progress.mapper;

import com.example.lms.content.model.Content;
import com.example.lms.progress.dto.ContentProgressDTO;
import com.example.lms.progress.model.ContentProgress;
import com.example.lms.user.model.User;

public class ContentProgressMapper {

    // Convert ContentProgressDTO to ContentProgress entity
    public static ContentProgress toEntity(ContentProgressDTO dto, User student, Content content) {
        // Create a new ContentProgress entity from the DTO
        ContentProgress contentProgress = new ContentProgress();

        // Set fields
        contentProgress.setStudent(student);  // Set student directly
        contentProgress.setContent(content);  // Setting content directly
        contentProgress.setProgress(dto.getProgress());
        contentProgress.setLastUpdated(dto.getLastUpdated());

        return contentProgress;
    }

    // Convert ContentProgress entity to ContentProgressDTO
    public static ContentProgressDTO toDTO(ContentProgress contentProgress) {
        // Convert the entity to a DTO
        return ContentProgressDTO.builder()
                .id(contentProgress.getId())
                .studentId(contentProgress.getStudent().getId())  // Get student ID directly
                .contentId(contentProgress.getContent().getId())  // Accessing content
                .progress(contentProgress.getProgress())
                .lastUpdated(contentProgress.getLastUpdated())
                .build();
    }
}
