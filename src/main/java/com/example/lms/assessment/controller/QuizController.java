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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Management", description = "API endpoints for managing quizzes")
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
    @Operation(summary = "Create a new quiz", description = "Create a new quiz with the specified details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz created successfully",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
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
    @Operation(summary = "Get quiz by ID", description = "Retrieve a quiz by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz found",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get quiz with questions", description = "Retrieve a quiz with all its questions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz found",
                    content = @Content(schema = @Schema(implementation = QuizDTO.DetailedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get all quizzes for a course", description = "Retrieve all quizzes for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quizzes found",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
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
    @Operation(summary = "Get all published quizzes for a course", description = "Retrieve all published quizzes for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Published quizzes found",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
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
    @Operation(summary = "Get all available quizzes for a course", description = "Retrieve all currently available quizzes for a specific course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available quizzes found",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
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
    @Operation(summary = "Get quiz for student", description = "Retrieve a quiz with limited information for a specific student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz found",
                    content = @Content(schema = @Schema(implementation = QuizDTO.StudentView.class))),
            @ApiResponse(responseCode = "404", description = "Quiz or student not found")
    })
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
    @Operation(summary = "Update an existing quiz", description = "Update the details of an existing quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz updated successfully",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Delete a quiz", description = "Delete a quiz by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quiz deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Publish a quiz", description = "Publish a quiz to make it available to students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz published successfully",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Unpublish a quiz", description = "Unpublish a quiz to hide it from students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz unpublished successfully",
                    content = @Content(schema = @Schema(implementation = QuizDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get quiz analytics", description = "Retrieve analytics for a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz analytics found",
                    content = @Content(schema = @Schema(implementation = QuizAnalyticsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get quiz difficulty", description = "Retrieve the difficulty level of a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz difficulty found",
                    content = @Content(schema = @Schema(implementation = Double.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get quiz pass rate", description = "Retrieve the pass rate for a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz pass rate found",
                    content = @Content(schema = @Schema(implementation = Double.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<Double> getQuizPassRate(@PathVariable Long id) {
        Double passRate = quizAnalyticsService.calculatePassRate(id);
        return ResponseEntity.ok(passRate);
    }
}