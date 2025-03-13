package com.example.lms.assessment.controller;

import com.example.lms.assessment.dto.QuestionDTO;
import com.example.lms.assessment.service.QuestionService;
import com.example.lms.assessment.service.QuizAnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final QuizAnalyticsService quizAnalyticsService;

    /**
     * Create a new question for a quiz
     *
     * @param questionDTO Question data
     * @return Created question
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuestionDTO.Response> createQuestion(@Valid @RequestBody QuestionDTO.Request questionDTO) {
        QuestionDTO.Response createdQuestion = questionService.createQuestion(questionDTO);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    /**
     * Get a question by ID
     *
     * @param id Question ID
     * @return Question with the given ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuestionDTO.Response> getQuestionById(@PathVariable Long id) {
        QuestionDTO.Response question = questionService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }

    /**
     * Get all questions for a quiz
     *
     * @param quizId Quiz ID
     * @return List of questions for the quiz
     */
    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuestionDTO.Response>> getQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuestionDTO.Response> questions = questionService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Get student view of all questions for a quiz
     * (without revealing correct answers)
     *
     * @param quizId Quiz ID
     * @return List of questions for students
     */
    @GetMapping("/quiz/{quizId}/student")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuestionDTO.StudentView>> getQuestionsForStudent(@PathVariable Long quizId) {
        List<QuestionDTO.StudentView> questions = questionService.getQuestionsForStudent(quizId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Update an existing question
     *
     * @param id Question ID
     * @param questionDTO Updated question data
     * @return Updated question
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuestionDTO.Response> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionDTO.Request questionDTO) {
        QuestionDTO.Response updatedQuestion = questionService.updateQuestion(id, questionDTO);
        return ResponseEntity.ok(updatedQuestion);
    }

    /**
     * Delete a question
     *
     * @param id Question ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder questions in a quiz
     *
     * @param quizId Quiz ID
     * @param questionIds Ordered list of question IDs
     * @return Updated list of questions in new order
     */
    @PutMapping("/quiz/{quizId}/reorder")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuestionDTO.Response>> reorderQuestions(
            @PathVariable Long quizId,
            @RequestBody List<Long> questionIds) {
        List<QuestionDTO.Response> reorderedQuestions = questionService.reorderQuestions(quizId, questionIds);
        return ResponseEntity.ok(reorderedQuestions);
    }
    
    /**
     * Get difficulty level of a question
     *
     * @param id Question ID
     * @return Difficulty level (0-100)
     */
    @GetMapping("/{id}/difficulty")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Double> getQuestionDifficulty(@PathVariable Long id) {
        Double difficulty = quizAnalyticsService.calculateQuestionDifficulty(id);
        return ResponseEntity.ok(difficulty);
    }
}