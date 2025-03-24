package com.example.lms.assignment.assessment.mapper;

import com.example.lms.assignment.assessment.dto.AssignmentDTO;
import com.example.lms.assignment.assessment.dto.ScoreDTO;
import com.example.lms.assignment.assessment.model.Assignment;
import com.example.lms.assignment.assessment.model.Score;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {
    AssignmentDTO toAssignmentDTO(Assignment assignment);
    Assignment toAssignment(AssignmentDTO assignmentDTO);

    ScoreDTO toScoreDTO(Score score);
    Score toScore(ScoreDTO scoreDTO);
}