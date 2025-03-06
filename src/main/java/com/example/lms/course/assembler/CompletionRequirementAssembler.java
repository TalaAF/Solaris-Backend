package com.example.lms.course.assembler;

import com.example.lms.course.dto.CompletionRequirementDTO;
import com.example.lms.course.model.CompletionRequirement;
import com.example.lms.course.mapper.CompletionRequirementMapper;
import org.springframework.stereotype.Component;

@Component
public class CompletionRequirementAssembler {

    // Convert DTO to Entity
    public CompletionRequirement toEntity(CompletionRequirementDTO dto) {
        return CompletionRequirementMapper.toEntity(dto);
    }

    // Convert Entity to DTO
    public CompletionRequirementDTO toDTO(CompletionRequirement completionRequirement) {
        return CompletionRequirementMapper.toDTO(completionRequirement);
    }
}
