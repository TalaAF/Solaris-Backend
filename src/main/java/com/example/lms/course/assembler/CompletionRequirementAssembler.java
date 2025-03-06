package com.example.lms.course.assembler;

import com.example.lms.course.dto.CompletionRequirementDTO;
import com.example.lms.course.model.CompletionRequirement;
import com.example.lms.course.mapper.CompletionRequirementMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    // Convert list of Entities to list of DTOs
    public List<CompletionRequirementDTO> toDTO(List<CompletionRequirement> completionRequirements) {
        return completionRequirements.stream()
                                     .map(this::toDTO)
                                     .collect(Collectors.toList());
    }
}
