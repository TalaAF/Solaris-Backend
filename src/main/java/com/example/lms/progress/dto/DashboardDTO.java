package com.example.lms.progress.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardDTO {
    private Double overallProgress;
    private Integer totalCourses;
    private Integer completedCourses;
    private Integer inProgressCourses;
    private Integer notStartedCourses;
    private LocalDateTime lastUpdated;
    private List<ProgressDTO> courseProgress;
}