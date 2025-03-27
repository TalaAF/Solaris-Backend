package com.example.lms.assignment.assignments.mapper;

import org.mapstruct.Mapper;

import com.example.lms.assignment.assignments.dto.AssignmentDTO;
import com.example.lms.assignment.assignments.dto.ScoreDTO;
import com.example.lms.assignment.assignments.model.Assignment;
import com.example.lms.assignment.assignments.model.Score;

@Mapper(componentModel = "spring" )
public interface AssessmentMapper {
    AssignmentDTO toAssignmentDTO(Assignment assignment);
    Assignment toAssignment(AssignmentDTO assignmentDTO);

    ScoreDTO toScoreDTO(Score score);
    Score toScore(ScoreDTO scoreDTO);
}