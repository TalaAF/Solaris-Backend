package com.example.lms.course.controller;

import com.example.lms.course.dto.CompletionRequirementDTO;
import com.example.lms.course.model.CompletionRequirement;
import com.example.lms.course.service.CompletionVerificationService;
import com.example.lms.course.assembler.CompletionRequirementAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/completion-requirements")
public class CompletionRequirementController {

    @Autowired
    private CompletionVerificationService completionVerificationService; // Corrected the service name

    @Autowired
    private CompletionRequirementAssembler completionRequirementAssembler;

    // Get all completion requirements for a course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CompletionRequirementDTO>> getCompletionRequirements(@PathVariable Long courseId) {
        List<CompletionRequirement> completionRequirements = completionVerificationService.getCompletionRequirementsForCourse(courseId);
        List<CompletionRequirementDTO> dtos = completionRequirementAssembler.toDTO(completionRequirements);
        return ResponseEntity.ok(dtos);
    }

    // Create a new completion requirement for a course
    @PostMapping("/course/{courseId}")
    public ResponseEntity<CompletionRequirementDTO> createCompletionRequirement(
            @PathVariable Long courseId,
            @RequestBody CompletionRequirementDTO completionRequirementDTO) {
        
        CompletionRequirement completionRequirement = completionRequirementAssembler.toEntity(completionRequirementDTO);
        CompletionRequirement createdRequirement = completionVerificationService.createCompletionRequirement(courseId, completionRequirement);
        CompletionRequirementDTO createdDTO = completionRequirementAssembler.toDTO(createdRequirement);
        
        return ResponseEntity.ok(createdDTO);
    }
}
