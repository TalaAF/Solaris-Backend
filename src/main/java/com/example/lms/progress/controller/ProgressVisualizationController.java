package com.example.lms.progress.controller;

import com.example.lms.progress.service.ProgressVisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress-visualization")
public class ProgressVisualizationController {

    @Autowired
    private ProgressVisualizationService progressVisualizationService;

    @GetMapping("/overall/{studentId}")
    public ResponseEntity<Double> getOverallProgress(@PathVariable Long studentId) {
        Double overallProgress = progressVisualizationService.calculateOverallProgress(studentId);
        return ResponseEntity.ok(overallProgress);
    }
}
