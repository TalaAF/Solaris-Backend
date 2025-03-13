package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.QuizAnalyticsDTO;

/**
 * Service for generating analytics and statistics for quizzes
 */
public interface QuizAnalyticsService {
    
    /**
     * Generate comprehensive analytics for a quiz
     *
     * @param quizId Quiz ID
     * @return Quiz analytics
     */
    QuizAnalyticsDTO generateQuizAnalytics(Long quizId);
    
    /**
     * Get difficulty level of a quiz based on student performance
     *
     * @param quizId Quiz ID
     * @return Difficulty level (0-100, where 100 is most difficult)
     */
    Double calculateQuizDifficulty(Long quizId);
    
    /**
     * Get quiz completion rate
     *
     * @param quizId Quiz ID
     * @return Completion rate (0-100%)
     */
    Double calculateCompletionRate(Long quizId);
    
    /**
     * Get quiz pass rate
     *
     * @param quizId Quiz ID
     * @return Pass rate (0-100%)
     */
    Double calculatePassRate(Long quizId);
    
    /**
     * Get average time taken to complete the quiz
     *
     * @param quizId Quiz ID
     * @return Average minutes to complete
     */
    Double calculateAverageTimeToComplete(Long quizId);
    
    /**
     * Calculate question difficulty
     *
     * @param questionId Question ID
     * @return Difficulty level (0-100, where 100 is most difficult)
     */
    Double calculateQuestionDifficulty(Long questionId);
}