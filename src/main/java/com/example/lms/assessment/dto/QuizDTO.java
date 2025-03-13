package com.example.lms.assessment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Quiz title is required")
        private String title;
        
        private String description;
        
        @Min(value = 1, message = "Time limit must be at least 1 minute")
        private Integer timeLimit;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDate;
        
        @Min(value = 0, message = "Passing score must be at least 0")
        private Double passingScore;
        
        private boolean randomizeQuestions;
        
        private boolean published;
        
        @NotNull(message = "Course ID is required")
        private Long courseId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Integer timeLimit;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDate;
        
        private Double passingScore;
        private boolean randomizeQuestions;
        private boolean published;
        private Long courseId;
        private String courseName;
        private Integer questionCount;
        private Integer totalPossibleScore;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedResponse {
        private Long id;
        private String title;
        private String description;
        private Integer timeLimit;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDate;
        
        private Double passingScore;
        private boolean randomizeQuestions;
        private boolean published;
        private Long courseId;
        private String courseName;
        private Integer totalPossibleScore;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        
        private List<QuestionDTO.Response> questions = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentView {
        private Long id;
        private String title;
        private String description;
        private Integer timeLimit;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endDate;
        
        private Double passingScore;
        private Long courseId;
        private String courseName;
        private Integer questionCount;
        private Integer totalPossibleScore;
        private boolean attempted;
        private boolean completed;
        private Double highestScore;
        private boolean passed;
    }
}