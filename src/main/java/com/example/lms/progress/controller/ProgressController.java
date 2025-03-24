package com.example.lms.progress.controller;

import com.example.lms.progress.dto.ProgressDTO;
import com.example.lms.progress.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @PutMapping("/update/{studentId}/{courseId}")
    public ResponseEntity<ProgressDTO> updateProgress(
            @PathVariable Long studentId, 
            @PathVariable Long courseId, 
            @RequestParam Double progress) {
        
        ProgressDTO updatedProgress = progressService.updateProgress(studentId, courseId, progress);
        return ResponseEntity.ok(updatedProgress);
    }

    @GetMapping("/{studentId}/{courseId}")
    public ResponseEntity<ProgressDTO> getProgress(
            @PathVariable Long studentId, 
            @PathVariable Long courseId) {
        
        ProgressDTO progress = progressService.getProgress(studentId, courseId);
        return ResponseEntity.ok(progress);
    }
}

