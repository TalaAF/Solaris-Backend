package com.example.lms.assignment.assessment.mapper;

import com.example.lms.assignment.assessment.dto.AssignmentDTO;
import com.example.lms.assignment.assessment.dto.QuizDTO;
import com.example.lms.assignment.assessment.dto.ScoreDTO;
import com.example.lms.assignment.assessment.model.Assignment;
import com.example.lms.assignment.assessment.model.Quiz;
import com.example.lms.assignment.assessment.model.Score;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {
    QuizDTO toQuizDTO(Quiz quiz);
    Quiz toQuiz(QuizDTO quizDTO);

    AssignmentDTO toAssignmentDTO(Assignment assignment);
    Assignment toAssignment(AssignmentDTO assignmentDTO);

    ScoreDTO toScoreDTO(Score score);
    Score toScore(ScoreDTO scoreDTO);
}