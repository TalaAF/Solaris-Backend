package com.example.lms.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for course statistics.
 * Contains metrics and information about course performance and usage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticsDTO {
    private Long courseId;
    private String courseName;
    private Integer totalStudents;
    private Integer totalContent;
    private Integer totalQuizzes;
    private Double averageCompletionPercentage;
    private Integer completedCount;
    private Integer inProgressCount;
    private Double averageQuizScore;
}