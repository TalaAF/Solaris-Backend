package com.example.lms.assignment.submission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {
    private Long id;
    private Long assignmentId;
    private Long userId;
    private String userFullName;
    private String submissionText;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
    
    private Double grade;
    private String feedback;
    private String[] attachmentUrls;
}