package com.example.lms.progress.assembler;

import com.example.lms.progress.dto.ProgressDTO;
import com.example.lms.progress.model.Progress;
import com.example.lms.course.model.Course;
import com.example.lms.user.model.User;

import java.time.LocalDateTime;

public class ProgressAssembler {

    public static ProgressDTO toDTO(Progress progress) {
        ProgressDTO dto = new ProgressDTO();
        dto.setStudentId(progress.getStudent().getId());
        dto.setCourseId(progress.getCourse().getId());
        dto.setProgress(progress.getProgress());
        dto.setLastUpdated(progress.getLastUpdated());
        return dto;
    }

    public static Progress toEntity(ProgressDTO dto, User student, Course course) {
        return new Progress(student, course, dto.getProgress(), LocalDateTime.now());
    }
}
