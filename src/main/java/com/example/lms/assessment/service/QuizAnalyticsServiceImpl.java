package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.QuizAnalyticsDTO;
import com.example.lms.assessment.model.*;
import com.example.lms.assessment.repository.*;
import com.example.lms.common.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAnalyticsServiceImpl implements QuizAnalyticsService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    @Override
    @Transactional(readOnly = true)
    public QuizAnalyticsDTO generateQuizAnalytics(Long quizId) {
        // Verify quiz exists
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        // Get basic quiz metrics
        Long totalAttempts = quizAttemptRepository.countByQuizId(quizId);
        Long completedAttempts = quizAttemptRepository.countCompletedAttempts(quizId);
        Double averageScore = quizAttemptRepository.calculateAverageScore(quizId);
        
        // Calculate pass rate
        Long passedCount = quizAttemptRepository.countPassedAttempts(quizId);
        Long failedCount = completedAttempts - passedCount;
        Double passRate = completedAttempts > 0 ? (double) passedCount / completedAttempts * 100 : 0.0;
        
        // Create score distribution
        Map<String, Double> scoreDistribution = generateScoreDistribution(quizId);
        
        // Generate question analytics
        List<QuizAnalyticsDTO.QuestionAnalyticsDTO> questionAnalytics = generateQuestionAnalytics(quizId);
        
        // Build the complete analytics DTO
        return QuizAnalyticsDTO.builder()
                .quizId(quizId)
                .quizTitle(quiz.getTitle())
                .totalAttempts(totalAttempts)
                .completedAttempts(completedAttempts)
                .averageScore(averageScore != null ? averageScore : 0.0)
                .passRate(passRate)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .scoreDistribution(scoreDistribution)
                .questionAnalytics(questionAnalytics)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateQuizDifficulty(Long quizId) {
        // Verify quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        
        // Calculate average score as inverse indicator of difficulty (higher score = lower difficulty)
        Double averageScore = quizAttemptRepository.calculateAverageScore(quizId);
        
        // If no attempts, return neutral difficulty
        if (averageScore == null) {
            return 50.0;
        }
        
        // Convert to difficulty (100 - average percentage score)
        // This way, 0 is easiest, 100 is hardest
        return 100.0 - averageScore;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateCompletionRate(Long quizId) {
        // Verify quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        
        Long totalAttempts = quizAttemptRepository.countByQuizId(quizId);
        Long completedAttempts = quizAttemptRepository.countCompletedAttempts(quizId);
        
        if (totalAttempts == 0) {
            return 0.0;
        }
        
        return (double) completedAttempts / totalAttempts * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculatePassRate(Long quizId) {
        // Verify quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        
        Long completedAttempts = quizAttemptRepository.countCompletedAttempts(quizId);
        Long passedAttempts = quizAttemptRepository.countPassedAttempts(quizId);
        
        if (completedAttempts == 0) {
            return 0.0;
        }
        
        return (double) passedAttempts / completedAttempts * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageTimeToComplete(Long quizId) {
        // Verify quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        
        // Get all completed attempts
        List<QuizAttempt> completedAttempts = quizAttemptRepository.findByQuizId(quizId).stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED && attempt.getSubmittedAt() != null)
                .collect(Collectors.toList());
        
        if (completedAttempts.isEmpty()) {
            return 0.0;
        }
        
        // Calculate average duration in minutes
        double totalMinutes = 0.0;
        for (QuizAttempt attempt : completedAttempts) {
            Duration duration = Duration.between(attempt.getStartedAt(), attempt.getSubmittedAt());
            totalMinutes += duration.toMinutes();
        }
        
        return totalMinutes / completedAttempts.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateQuestionDifficulty(Long questionId) {
        // Verify question exists
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found with id: " + questionId);
        }
        
        // Calculate percentage of correct answers
        Long totalAnswers = studentAnswerRepository.countTotalAnswers(questionId);
        Long correctAnswers = studentAnswerRepository.countCorrectAnswers(questionId);
        
        if (totalAnswers == 0) {
            return 50.0; // Neutral difficulty if no data
        }
        
        // Difficulty = 100 - percentage correct
        Double percentageCorrect = (double) correctAnswers / totalAnswers * 100;
        return 100.0 - percentageCorrect;
    }
    
    // Helper method to generate score distribution (for charts)
    private Map<String, Double> generateScoreDistribution(Long quizId) {
        Map<String, Double> distribution = new LinkedHashMap<>();
        
        // Define score ranges
        String[] ranges = {
            "0-9", "10-19", "20-29", "30-39", "40-49", 
            "50-59", "60-69", "70-79", "80-89", "90-100"
        };
        
        // Initialize all ranges with 0
        for (String range : ranges) {
            distribution.put(range, 0.0);
        }
        
        // Get completed attempts
        List<QuizAttempt> completedAttempts = quizAttemptRepository.findByQuizId(quizId).stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED)
                .collect(Collectors.toList());
        
        if (completedAttempts.isEmpty()) {
            return distribution;
        }
        
        // Count attempts in each range
        Map<String, Integer> counts = new HashMap<>();
        for (String range : ranges) {
            counts.put(range, 0);
        }
        
        for (QuizAttempt attempt : completedAttempts) {
            Double score = attempt.getPercentageScore();
            String range = getScoreRange(score, ranges);
            counts.put(range, counts.get(range) + 1);
        }
        
        // Convert to percentages
        int total = completedAttempts.size();
        for (String range : ranges) {
            distribution.put(range, (double) counts.get(range) / total * 100);
        }
        
        return distribution;
    }
    
    // Helper method to determine score range
    private String getScoreRange(Double score, String[] ranges) {
        if (score == null) return ranges[0];
        
        int rangeIndex = Math.min(9, (int) (score / 10));
        return ranges[rangeIndex];
    }
    
    // Helper method to generate question analytics
    private List<QuizAnalyticsDTO.QuestionAnalyticsDTO> generateQuestionAnalytics(Long quizId) {
        // Get all questions for the quiz
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        
        return questions.stream().map(question -> {
            Long questionId = question.getId();
            
            // Get answer statistics
            Long totalAnswers = studentAnswerRepository.countTotalAnswers(questionId);
            Long correctAnswers = studentAnswerRepository.countCorrectAnswers(questionId);
            Double correctPercentage = totalAnswers > 0 ? 
                    (double) correctAnswers / totalAnswers * 100 : 0.0;
            
            // Generate option analytics
            List<QuizAnalyticsDTO.OptionAnalyticsDTO> optionAnalytics = 
                    generateOptionAnalytics(questionId, totalAnswers);
            
            return QuizAnalyticsDTO.QuestionAnalyticsDTO.builder()
                    .questionId(questionId)
                    .questionText(question.getText())
                    .correctPercentage(correctPercentage)
                    .totalAnswers(totalAnswers)
                    .correctAnswers(correctAnswers)
                    .optionAnalytics(optionAnalytics)
                    .build();
        }).collect(Collectors.toList());
    }
    
    // Helper method to generate option analytics
    private List<QuizAnalyticsDTO.OptionAnalyticsDTO> generateOptionAnalytics(Long questionId, Long totalAnswers) {
        // Get all options for the question
        List<AnswerOption> options = answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(questionId);
        
        return options.stream().map(option -> {
            Long optionId = option.getId();
            
            // Count how many times this option was selected
            Long timesSelected = answerOptionRepository.countTimesSelected(optionId);
            
            // Calculate selection percentage
            Double selectionPercentage = totalAnswers > 0 ? 
                    (double) timesSelected / totalAnswers * 100 : 0.0;
            
            return QuizAnalyticsDTO.OptionAnalyticsDTO.builder()
                    .optionId(optionId)
                    .optionText(option.getText())
                    .isCorrect(option.isCorrect())
                    .timesSelected(timesSelected)
                    .selectionPercentage(selectionPercentage)
                    .build();
        }).collect(Collectors.toList());
    }
}