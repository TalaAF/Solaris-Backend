package com.example.lms.progress.mapper;

import com.example.lms.progress.dto.ProgressDTO;
import com.example.lms.progress.model.Progress;

public class ProgressMapper {

    public static ProgressDTO toDTO(Progress progress) {
        ProgressDTO dto = new ProgressDTO();
        dto.setStudentId(progress.getStudent().getId());
        dto.setCourseId(progress.getCourse().getId());
        dto.setProgress(progress.getProgress());
        dto.setLastUpdated(progress.getLastUpdated());
        return dto;
    }

    public static Progress toEntity(ProgressDTO dto) {
        // Assuming User and Course are being passed as needed.
        // This is usually done in the service layer.
        return new Progress(dto.getStudentId(), dto.getCourseId(), dto.getProgress(), dto.getLastUpdated());
    }
}
