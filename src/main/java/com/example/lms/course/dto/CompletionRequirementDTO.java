package com.example.lms.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRequirementDTO {

    private Long id;
    private Long courseId; // The course to which this requirement belongs
    private Double requiredProgress; // Required percentage of progress to complete the course
    private Boolean quizPassedRequired; // Whether passing a quiz is required
}
