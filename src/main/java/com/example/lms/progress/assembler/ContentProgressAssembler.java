package com.example.lms.progress.assembler;

import com.example.lms.progress.dto.ContentProgressDTO;
import com.example.lms.progress.mapper.ContentProgressMapper;
import com.example.lms.progress.model.ContentProgress;
import com.example.lms.content.model.Content;
import com.example.lms.enrollment.model.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class ContentProgressAssembler {

    // Convert DTO to Entity
    public ContentProgress toEntity(ContentProgressDTO dto, Enrollment enrollment, Content content) {
        return ContentProgressMapper.toEntity(dto, enrollment, content);
    }

    // Convert Entity to DTO
    public ContentProgressDTO toDTO(ContentProgress contentProgress) {
        return ContentProgressDTO.builder()
                .id(contentProgress.getId())
                .studentId(contentProgress.getEnrollment().getStudent().getId()) // Access student correctly
                .contentId(contentProgress.getContent().getId())
                .progress(contentProgress.getProgress())
                .lastUpdated(contentProgress.getLastUpdated()) // Now this should work
                .build();
    }
}
