package com.example.lms.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnalyticsDTO {
    private Long quizId;
    private String quizTitle;
    private Long totalAttempts;
    private Long completedAttempts;
    private Double averageScore;
    private Double passRate;
    private Long passedCount;
    private Long failedCount;
    
    @Builder.Default
    private List<QuestionAnalyticsDTO> questionAnalytics = new ArrayList<>();
    
    @Builder.Default
    private Map<String, Double> scoreDistribution = new HashMap<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnalyticsDTO {
        private Long questionId;
        private String questionText;
        private Double correctPercentage;
        private Long totalAnswers;
        private Long correctAnswers;
        private List<OptionAnalyticsDTO> optionAnalytics = new ArrayList<>();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionAnalyticsDTO {
        private Long optionId;
        private String optionText;
        private boolean isCorrect;
        private Long timesSelected;
        private Double selectionPercentage;
    }
}