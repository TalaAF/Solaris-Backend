package com.example.lms.enrollment.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchEnrollmentRequest {
    private Long courseId;
    private List<Long> userIds;
}