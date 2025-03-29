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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Question Management", description = "API endpoints for managing quiz questions")
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
    @Operation(summary = "Create a new question", description = "Create a new question for a quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Question created successfully",
                    content = @Content(schema = @Schema(implementation = QuestionDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
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
    @Operation(summary = "Get question by ID", description = "Retrieve a question by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question found",
                    content = @Content(schema = @Schema(implementation = QuestionDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
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
    @Operation(summary = "Get all questions for a quiz", description = "Retrieve all questions for a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions found",
                    content = @Content(schema = @Schema(implementation = QuestionDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get student view of questions for a quiz", description = "Retrieve all questions for a specific quiz without correct answers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions found",
                    content = @Content(schema = @Schema(implementation = QuestionDTO.StudentView.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Update an existing question", description = "Update an existing question by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question updated",
                    content = @Content(schema = @Schema(implementation = QuestionDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
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
    @Operation(summary = "Delete a question", description = "Delete a question by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Question deleted"),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
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
    @Operation(summary = "Reorder questions in a quiz", description = "Reorder questions in a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions reordered",
                    content = @Content(schema = @Schema(implementation = QuestionDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
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
    @Operation(summary = "Get question difficulty", description = "Calculate the difficulty level of a question")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Difficulty level calculated",
                    content = @Content(schema = @Schema(implementation = Double.class))),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
    public ResponseEntity<Double> getQuestionDifficulty(@PathVariable Long id) {
        Double difficulty = quizAnalyticsService.calculateQuestionDifficulty(id);
        return ResponseEntity.ok(difficulty);
    }
}