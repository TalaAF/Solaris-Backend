package com.example.lms.assessment.controller;

import com.example.lms.assessment.dto.QuizDTO;
import com.example.lms.assessment.dto.QuizAnalyticsDTO;
import com.example.lms.assessment.service.QuizAnalyticsService;
import com.example.lms.assessment.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizAnalyticsService quizAnalyticsService;

    /**
     * Create a new quiz
     *
     * @param quizDTO Quiz data
     * @return Created quiz
     */
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizDTO.Response> createQuiz(@Valid @RequestBody QuizDTO.Request quizDTO) {
        QuizDTO.Response createdQuiz = quizService.createQuiz(quizDTO);
        return new ResponseEntity<>(createdQuiz, HttpStatus.CREATED);
    }

    /**
     * Get a quiz by ID
     *
     * @param id Quiz ID
     * @return Quiz with the given ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizDTO.Response> getQuizById(@PathVariable Long id) {
        QuizDTO.Response quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Get a quiz with detailed information including all questions
     *
     * @param id Quiz ID
     * @return Detailed quiz with questions
     */
    @GetMapping("/{id}/detailed")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizDTO.DetailedResponse> getQuizWithQuestions(@PathVariable Long id) {
        QuizDTO.DetailedResponse quiz = quizService.getQuizWithQuestions(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Get all quizzes for a course
     *
     * @param courseId Course ID
     * @return List of quizzes for the course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizDTO.Response>> getQuizzesByCourse(@PathVariable Long courseId) {
        List<QuizDTO.Response> quizzes = quizService.getQuizzesByCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Get all published quizzes for a course
     *
     * @param courseId Course ID
     * @return List of published quizzes for the course
     */
    @GetMapping("/course/{courseId}/published")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizDTO.Response>> getPublishedQuizzesByCourse(@PathVariable Long courseId) {
        List<QuizDTO.Response> quizzes = quizService.getPublishedQuizzesByCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Get all currently available quizzes for a course (student view)
     *
     * @param courseId Course ID
     * @return List of available quizzes for the course
     */
    @GetMapping("/course/{courseId}/available")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizDTO.Response>> getAvailableQuizzesByCourse(@PathVariable Long courseId) {
        List<QuizDTO.Response> quizzes = quizService.getAvailableQuizzesByCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Get a student view of a quiz (with limited information)
     *
     * @param quizId Quiz ID
     * @param studentId Student ID
     * @return Student view of the quiz
     */
    @GetMapping("/{quizId}/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizDTO.StudentView> getQuizForStudent(
            @PathVariable Long quizId,
            @PathVariable Long studentId) {
        QuizDTO.StudentView quiz = quizService.getQuizForStudent(quizId, studentId);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Update an existing quiz
     *
     * @param id Quiz ID
     * @param quizDTO Updated quiz data
     * @return Updated quiz
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizDTO.Response> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizDTO.Request quizDTO) {
        QuizDTO.Response updatedQuiz = quizService.updateQuiz(id, quizDTO);
        return ResponseEntity.ok(updatedQuiz);
    }

    /**
     * Delete a quiz
     *
     * @param id Quiz ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish a quiz (make it available to students)
     *
     * @param id Quiz ID
     * @return Published quiz
     */
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizDTO.Response> publishQuiz(@PathVariable Long id) {
        QuizDTO.Response publishedQuiz = quizService.publishQuiz(id);
        return ResponseEntity.ok(publishedQuiz);
    }

    /**
     * Unpublish a quiz (hide it from students)
     *
     * @param id Quiz ID
     * @return Unpublished quiz
     */
    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizDTO.Response> unpublishQuiz(@PathVariable Long id) {
        QuizDTO.Response unpublishedQuiz = quizService.unpublishQuiz(id);
        return ResponseEntity.ok(unpublishedQuiz);
    }
    
    /**
     * Get analytics for a quiz
     *
     * @param id Quiz ID
     * @return Quiz analytics
     */
    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<QuizAnalyticsDTO> getQuizAnalytics(@PathVariable Long id) {
        QuizAnalyticsDTO analytics = quizAnalyticsService.generateQuizAnalytics(id);
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * Get difficulty level of a quiz
     *
     * @param id Quiz ID
     * @return Difficulty level (0-100)
     */
    @GetMapping("/{id}/difficulty")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Double> getQuizDifficulty(@PathVariable Long id) {
        Double difficulty = quizAnalyticsService.calculateQuizDifficulty(id);
        return ResponseEntity.ok(difficulty);
    }
    
    /**
     * Get pass rate for a quiz
     *
     * @param id Quiz ID
     * @return Pass rate (0-100%)
     */
    @GetMapping("/{id}/pass-rate")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Double> getQuizPassRate(@PathVariable Long id) {
        Double passRate = quizAnalyticsService.calculatePassRate(id);
        return ResponseEntity.ok(passRate);
    }
}