package com.example.lms.progress.assembler;

import com.example.lms.content.model.Content;
import com.example.lms.progress.dto.ContentProgressDTO;
import com.example.lms.progress.mapper.ContentProgressMapper;
import com.example.lms.progress.model.ContentProgress;
import com.example.lms.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class ContentProgressAssembler {

    // Convert DTO to Entity - Updated to use User instead of Enrollment
    public ContentProgress toEntity(ContentProgressDTO dto, User student, Content content) {
        return ContentProgressMapper.toEntity(dto, student, content);
    }

    // Convert Entity to DTO
    public ContentProgressDTO toDTO(ContentProgress contentProgress) {
        return ContentProgressDTO.builder()
                .id(contentProgress.getId())
                .studentId(contentProgress.getStudent().getId()) // Use getStudent() directly
                .contentId(contentProgress.getContent().getId())
                .progress(contentProgress.getProgress())
                .lastUpdated(contentProgress.getLastUpdated())
                .build();
    }
}
