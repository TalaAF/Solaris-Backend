package com.example.lms.course.mapper;

import com.example.lms.course.dto.CompletionRequirementDTO;
import com.example.lms.course.model.CompletionRequirement;

public class CompletionRequirementMapper {

    // Convert entity to DTO
    public static CompletionRequirementDTO toDTO(CompletionRequirement completionRequirement) {
        return CompletionRequirementDTO.builder()
                .id(completionRequirement.getId())
                .courseId(completionRequirement.getCourse().getId()) // Get the course ID
                .requiredProgress(completionRequirement.getRequiredProgress())
                .quizPassedRequired(completionRequirement.getQuizPassedRequired())
                .build();
    }

    // Convert DTO to entity
    public static CompletionRequirement toEntity(CompletionRequirementDTO dto) {
        CompletionRequirement completionRequirement = new CompletionRequirement();
        completionRequirement.setRequiredProgress(dto.getRequiredProgress());
        completionRequirement.setQuizPassedRequired(dto.getQuizPassedRequired());
        return completionRequirement;
    }
}
