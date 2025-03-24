package com.example.lms.assessment.dto;

import com.example.lms.assessment.model.AttemptStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizAttemptDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartRequest {
        @NotNull(message = "Quiz ID is required")
        private Long quizId;
        
        @NotNull(message = "Student ID is required")
        private Long studentId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitRequest {
        @NotNull(message = "Attempt ID is required")
        private Long attemptId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long quizId;
        private String quizTitle;
        private Long studentId;
        private String studentName;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startedAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime submittedAt;
        
        private Double score;
        private Double percentageScore;
        private boolean passed;
        private AttemptStatus status;
        private Integer totalQuestions;
        private Integer answeredQuestions;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedResponse {
        private Long id;
        private Long quizId;
        private String quizTitle;
        private Long studentId;
        private String studentName;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startedAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime submittedAt;
        
        private Double score;
        private Double percentageScore;
        private boolean passed;
        private AttemptStatus status;
        
        private List<StudentAnswerDTO.Response> answers = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InProgressAttempt {
        private Long id;
        private Long quizId;
        private String quizTitle;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startedAt;
        
        private Integer timeLimit;
        private AttemptStatus status;
        private List<QuestionDTO.AttemptQuestion> questions = new ArrayList<>();
    }
}