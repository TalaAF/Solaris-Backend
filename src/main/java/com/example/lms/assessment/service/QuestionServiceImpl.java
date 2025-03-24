package com.example.lms.assessment.service;

import com.example.lms.assessment.dto.AnswerOptionDTO;
import com.example.lms.assessment.dto.QuestionDTO;
import com.example.lms.assessment.model.AnswerOption;
import com.example.lms.assessment.model.Question;
import com.example.lms.assessment.model.QuestionType;
import com.example.lms.assessment.model.Quiz;
import com.example.lms.assessment.repository.AnswerOptionRepository;
import com.example.lms.assessment.repository.QuestionRepository;
import com.example.lms.assessment.repository.QuizRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizRepository quizRepository;

    @Override
    @Transactional
    public QuestionDTO.Response createQuestion(QuestionDTO.Request questionDTO) {
        // Fetch the quiz
        Quiz quiz = quizRepository.findById(questionDTO.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + questionDTO.getQuizId()));
        
        // Determine the next order index if not provided
        Integer orderIndex = questionDTO.getOrderIndex();
        if (orderIndex == null) {
            Integer maxOrderIndex = questionRepository.findMaxOrderIndexByQuizId(questionDTO.getQuizId());
            orderIndex = (maxOrderIndex != null) ? maxOrderIndex + 1 : 1;
        }
        
        // Create the question
        Question question = Question.builder()
                .text(questionDTO.getText())
                .type(questionDTO.getType())
                .points(questionDTO.getPoints())
                .orderIndex(orderIndex)
                .feedback(questionDTO.getFeedback())
                .quiz(quiz)
                .build();
        
        // Save the question first to get an ID
        Question savedQuestion = questionRepository.save(question);
        
        // Process answer options
        List<AnswerOption> options = new ArrayList<>();
        if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
            int optionIndex = 1;
            
            for (AnswerOptionDTO.Request optionDTO : questionDTO.getOptions()) {
                AnswerOption option = AnswerOption.builder()
                        .text(optionDTO.getText())
                        .isCorrect(optionDTO.getIsCorrect())
                        .feedback(optionDTO.getFeedback())
                        .orderIndex(optionDTO.getOrderIndex() != null ? optionDTO.getOrderIndex() : optionIndex++)
                        .question(savedQuestion)
                        .build();
                
                options.add(answerOptionRepository.save(option));
            }
        }
        
        // For TRUE_FALSE questions, ensure there are exactly two options (True and False)
        if (questionDTO.getType() == QuestionType.TRUE_FALSE && 
            (options.isEmpty() || options.size() != 2)) {
            
            // Clear existing options if any
            if (!options.isEmpty()) {
                for (AnswerOption option : options) {
                    answerOptionRepository.delete(option);
                }
                options.clear();
            }
            
            // Create True option
            AnswerOption trueOption = AnswerOption.builder()
                    .text("True")
                    .isCorrect(true) // Default to True being correct, can be changed with update
                    .orderIndex(1)
                    .question(savedQuestion)
                    .build();
            options.add(answerOptionRepository.save(trueOption));
            
            // Create False option
            AnswerOption falseOption = AnswerOption.builder()
                    .text("False")
                    .isCorrect(false)
                    .orderIndex(2)
                    .question(savedQuestion)
                    .build();
            options.add(answerOptionRepository.save(falseOption));
        }
        
        // Return the response DTO
        return mapToResponseDTO(savedQuestion, options);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionDTO.Response getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        
        List<AnswerOption> options = answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(id);
        
        return mapToResponseDTO(question, options);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO.Response> getQuestionsByQuizId(Long quizId) {
        // Check if quiz exists
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }
        
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        
        // Fetch all options for these questions in one query for efficiency
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        
        List<AnswerOption> allOptions = new ArrayList<>();
        if (!questionIds.isEmpty()) {
            allOptions = answerOptionRepository.findAll().stream()
                    .filter(option -> questionIds.contains(option.getQuestion().getId()))
                    .collect(Collectors.toList());
        }
        
        // Group options by question ID
        Map<Long, List<AnswerOption>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
        
        // Map questions to DTOs
        return questions.stream()
                .map(question -> {
                    List<AnswerOption> options = optionsByQuestionId.getOrDefault(question.getId(), new ArrayList<>());
                    options.sort(Comparator.comparing(AnswerOption::getOrderIndex));
                    
                    return mapToResponseDTO(question, options);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO.StudentView> getQuestionsForStudent(Long quizId) {
        // Check if quiz exists and is published
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        if (!quiz.isPublished()) {
            throw new ResourceNotFoundException("Quiz not found or not available");
        }
        
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        
        // If quiz should randomize questions, shuffle the list
        if (quiz.isRandomizeQuestions()) {
            java.util.Collections.shuffle(questions);
        }
        
        // Fetch all options for these questions in one query
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        
        List<AnswerOption> allOptions = new ArrayList<>();
        if (!questionIds.isEmpty()) {
            allOptions = answerOptionRepository.findAll().stream()
                    .filter(option -> questionIds.contains(option.getQuestion().getId()))
                    .collect(Collectors.toList());
        }
        
        // Group options by question ID
        Map<Long, List<AnswerOption>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
        
        // Map questions to student view DTOs
        return questions.stream()
                .map(question -> {
                    List<AnswerOption> options = optionsByQuestionId.getOrDefault(question.getId(), new ArrayList<>());
                    options.sort(Comparator.comparing(AnswerOption::getOrderIndex));
                    
                    return mapToStudentViewDTO(question, options);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestionDTO.Response updateQuestion(Long id, QuestionDTO.Request questionDTO) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        
        // Check if quiz is changing
        if (!question.getQuiz().getId().equals(questionDTO.getQuizId())) {
            Quiz newQuiz = quizRepository.findById(questionDTO.getQuizId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + questionDTO.getQuizId()));
            question.setQuiz(newQuiz);
        }
        
        // Update question fields
        question.setText(questionDTO.getText());
        question.setType(questionDTO.getType());
        question.setPoints(questionDTO.getPoints());
        question.setFeedback(questionDTO.getFeedback());
        
        // Update order index if provided
        if (questionDTO.getOrderIndex() != null) {
            question.setOrderIndex(questionDTO.getOrderIndex());
        }
        
        // Save updated question
        Question updatedQuestion = questionRepository.save(question);
        
        // Handle options update
        List<AnswerOption> currentOptions = answerOptionRepository.findByQuestionId(id);
        
        // If updating to TRUE_FALSE type, ensure proper options
        if (questionDTO.getType() == QuestionType.TRUE_FALSE) {
            // Clear existing options if not formatted correctly
            if (currentOptions.size() != 2 || 
                !currentOptions.stream().anyMatch(o -> o.getText().equalsIgnoreCase("True")) ||
                !currentOptions.stream().anyMatch(o -> o.getText().equalsIgnoreCase("False"))) {
                
                // Delete all current options
                answerOptionRepository.deleteByQuestionId(id);
                
                // Create True option
                AnswerOption trueOption = AnswerOption.builder()
                        .text("True")
                        .isCorrect(true) // Default, but will be updated below if needed
                        .orderIndex(1)
                        .question(updatedQuestion)
                        .build();
                answerOptionRepository.save(trueOption);
                
                // Create False option
                AnswerOption falseOption = AnswerOption.builder()
                        .text("False")
                        .isCorrect(false)
                        .orderIndex(2)
                        .question(updatedQuestion)
                        .build();
                answerOptionRepository.save(falseOption);
                
                // Reload current options
                currentOptions = answerOptionRepository.findByQuestionId(id);
            }
            
            // Update which option is correct based on the questionDTO
            if (questionDTO.getOptions() != null && !questionDTO.getOptions().isEmpty()) {
                // For TRUE_FALSE, we only care about which option is correct
                AnswerOption trueOption = currentOptions.stream()
                        .filter(o -> o.getText().equalsIgnoreCase("True"))
                        .findFirst().orElseThrow();
                AnswerOption falseOption = currentOptions.stream()
                        .filter(o -> o.getText().equalsIgnoreCase("False"))
                        .findFirst().orElseThrow();
                
                // Find which option should be correct
                boolean trueIsCorrect = questionDTO.getOptions().stream()
                        .anyMatch(o -> o.getText().equalsIgnoreCase("True") && Boolean.TRUE.equals(o.getIsCorrect()));
                
                // Update correctness
                trueOption.setCorrect(trueIsCorrect);
                falseOption.setCorrect(!trueIsCorrect);
                
                answerOptionRepository.save(trueOption);
                answerOptionRepository.save(falseOption);
            }
        } else if (questionDTO.getOptions() != null) {
            // For other question types, handle option updates
            
            // Create a map of current options by ID for easy lookup
            Map<Long, AnswerOption> currentOptionsMap = currentOptions.stream()
                    .collect(Collectors.toMap(AnswerOption::getId, Function.identity()));
            
            // Track which current options are still present in the update
            List<Long> remainingOptionIds = new ArrayList<>();
            
            // Process each option in the request
            int orderIndex = 1;
            for (AnswerOptionDTO.Request optionDTO : questionDTO.getOptions()) {
                if (optionDTO.getOrderIndex() != null) {
                    orderIndex = optionDTO.getOrderIndex();
                }
                
                // If has ID, update existing option
                if (optionDTO.getId() != null && currentOptionsMap.containsKey(optionDTO.getId())) {
                    AnswerOption option = currentOptionsMap.get(optionDTO.getId());
                    option.setText(optionDTO.getText());
                    option.setCorrect(optionDTO.getIsCorrect());
                    option.setFeedback(optionDTO.getFeedback());
                    option.setOrderIndex(orderIndex++);
                    answerOptionRepository.save(option);
                    remainingOptionIds.add(option.getId());
                } else {
                    // Create new option
                    AnswerOption option = AnswerOption.builder()
                            .text(optionDTO.getText())
                            .isCorrect(optionDTO.getIsCorrect())
                            .feedback(optionDTO.getFeedback())
                            .orderIndex(orderIndex++)
                            .question(updatedQuestion)
                            .build();
                    answerOptionRepository.save(option);
                }
            }
            
            // Remove options that weren't included in the update
            for (AnswerOption option : currentOptions) {
                if (!remainingOptionIds.contains(option.getId())) {
                    answerOptionRepository.delete(option);
                }
            }
        }
        
        // Refresh the question with updated options
        List<AnswerOption> updatedOptions = answerOptionRepository.findByQuestionIdOrderByOrderIndexAsc(id);
        
        return mapToResponseDTO(updatedQuestion, updatedOptions);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found with id: " + id);
        }
        
        // Delete all associated options first
        answerOptionRepository.deleteByQuestionId(id);
        
        // Delete the question
        questionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<QuestionDTO.Response> reorderQuestions(Long quizId, List<Long> questionIds) {
        // Validate quiz exists
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        
        // Get all questions for the quiz to validate IDs
        List<Question> existingQuestions = questionRepository.findByQuizId(quizId);
        
        // Create a map of question IDs to questions for easy lookup
        Map<Long, Question> questionMap = existingQuestions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        
        // Validate all provided IDs exist in the quiz
        for (Long questionId : questionIds) {
            if (!questionMap.containsKey(questionId)) {
                throw new ResourceNotFoundException("Question with id " + questionId + " not found in quiz " + quizId);
            }
        }
        
        // Reorder all questions
        for (int i = 0; i < questionIds.size(); i++) {
            Question question = questionMap.get(questionIds.get(i));
            question.setOrderIndex(i + 1);
            questionRepository.save(question);
        }
        
        // Return updated question list
        return getQuestionsByQuizId(quizId);
    }
    
    // Helper method to map Question entity to QuestionDTO.Response
    private QuestionDTO.Response mapToResponseDTO(Question question, List<AnswerOption> options) {
        // Map answer options
        List<AnswerOptionDTO.Response> optionDTOs = options.stream()
                .map(option -> AnswerOptionDTO.Response.builder()
                        .id(option.getId())
                        .text(option.getText())
                        .isCorrect(option.isCorrect())
                        .feedback(option.getFeedback())
                        .orderIndex(option.getOrderIndex())
                        .questionId(question.getId())
                        .build())
                .collect(Collectors.toList());
        
        // Build question response
        return QuestionDTO.Response.builder()
                .id(question.getId())
                .text(question.getText())
                .type(question.getType())
                .points(question.getPoints())
                .orderIndex(question.getOrderIndex())
                .feedback(question.getFeedback())
                .quizId(question.getQuiz().getId())
                .options(optionDTOs)
                .build();
    }
    
    // Helper method to map Question entity to QuestionDTO.StudentView
    private QuestionDTO.StudentView mapToStudentViewDTO(Question question, List<AnswerOption> options) {
        // Map answer options (without revealing which ones are correct)
        List<AnswerOptionDTO.StudentView> optionDTOs = options.stream()
                .map(option -> AnswerOptionDTO.StudentView.builder()
                        .id(option.getId())
                        .text(option.getText())
                        .orderIndex(option.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
        
        // Build student view of question
        return QuestionDTO.StudentView.builder()
                .id(question.getId())
                .text(question.getText())
                .type(question.getType())
                .points(question.getPoints())
                .orderIndex(question.getOrderIndex())
                .options(optionDTOs)
                .build();
    }
}