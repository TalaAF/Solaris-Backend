package com.example.lms.assessment.dto;

import com.example.lms.assessment.model.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class QuestionDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Question text is required")
        private String text;
        
        @NotNull(message = "Question type is required")
        private QuestionType type;
        
        @Min(value = 1, message = "Points must be at least 1")
        private Integer points;
        
        private Integer orderIndex;
        
        private String feedback;
        
        @NotNull(message = "Quiz ID is required")
        private Long quizId;
        
        private List<AnswerOptionDTO.Request> options = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String text;
        private QuestionType type;
        private Integer points;
        private Integer orderIndex;
        private String feedback;
        private Long quizId;
        private List<AnswerOptionDTO.Response> options = new ArrayList<>();
        
        // Analytics fields (optional)
        private Double correctPercentage;
        private Long timesAnswered;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentView {
        private Long id;
        private String text;
        private QuestionType type;
        private Integer points;
        private Integer orderIndex;
        private List<AnswerOptionDTO.StudentView> options = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptQuestion {
        private Long id;
        private String text;
        private QuestionType type;
        private Integer points;
        private Integer orderIndex;
        private List<AnswerOptionDTO.AttemptOption> options = new ArrayList<>();
        private String textAnswer; // For short answer/essay questions
    }
}