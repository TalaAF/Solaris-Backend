package com.example.lms.assessment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class StudentAnswerDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitRequest {
        @NotNull(message = "Attempt ID is required")
        private Long attemptId;
        
        @NotNull(message = "Question ID is required")
        private Long questionId;
        
        private List<Long> selectedOptionIds = new ArrayList<>();
        private String textAnswer;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long attemptId;
        private Long questionId;
        private String questionText;
        private String questionType;
        private List<AnswerOptionDTO.Response> selectedOptions = new ArrayList<>();
        private String textAnswer;
        private Double score;
        private boolean isCorrect;
        private boolean manuallyGraded;
        private String instructorFeedback;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeRequest {
        @NotNull(message = "Student answer ID is required")
        private Long studentAnswerId;
        
        @NotNull(message = "Score is required")
        private Double score;
        
        private String instructorFeedback;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentView {
        private Long id;
        private Long questionId;
        private String questionText;
        private String questionType;
        private List<AnswerOptionDTO.Response> selectedOptions = new ArrayList<>();
        private String textAnswer;
        private Double score;
        private boolean isCorrect;
        private String instructorFeedback;
    }
}