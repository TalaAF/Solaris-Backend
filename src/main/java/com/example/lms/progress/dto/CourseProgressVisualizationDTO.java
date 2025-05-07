package com.example.lms.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressVisualizationDTO {
    private ProgressItemDTO overall;
    private List<ProgressItemDTO> courses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressItemDTO {
        private String name;
        private Double percentage;
    }
}