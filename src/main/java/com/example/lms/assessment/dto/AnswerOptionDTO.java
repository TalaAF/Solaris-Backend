package com.example.lms.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AnswerOptionDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Option text is required")
        private String text;
        
        @NotNull(message = "Correctness flag is required")
        private Boolean isCorrect;
        
        private String feedback;
        private Integer orderIndex;
        private Long id;
      
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String text;
        private boolean isCorrect;
        private String feedback;
        private Integer orderIndex;
        private Long questionId;
        
        // Analytics fields (optional)
        private Long timesSelected;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentView {
        private Long id;
        private String text;
        private Integer orderIndex;
        // Correctness is omitted for student view
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptOption {
        private Long id;
        private String text;
        private Integer orderIndex;
        private boolean selected;
    }
}