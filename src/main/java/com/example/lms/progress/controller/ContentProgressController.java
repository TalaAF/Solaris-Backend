package com.example.lms.progress.controller;

import com.example.lms.progress.model.ContentProgress;
import com.example.lms.progress.service.ContentProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.lms.common.exception.ResourceNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/content-progress")
public class ContentProgressController {

    @Autowired
    private ContentProgressService contentProgressService;

    @PutMapping("/update/{studentId}/{contentId}")
    public ResponseEntity<ContentProgress> updateProgress(
            @PathVariable Long studentId, 
            @PathVariable Long contentId, 
            @RequestParam Double progress) {

        // Check for valid progress range
        if (progress < 0 || progress > 100) {
            return ResponseEntity.badRequest().body(null); // Return 400 Bad Request
        }
        
        try {
            ContentProgress updatedProgress = contentProgressService.updateProgress(studentId, contentId, progress);
            return ResponseEntity.ok(updatedProgress); // Return 200 OK with updated content progress
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 Not Found if no progress found
        }
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<List<ContentProgress>> getStudentProgress(@PathVariable Long studentId) {
        List<ContentProgress> progress = contentProgressService.getStudentProgress(studentId);
        
        if (progress.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if no progress found
        }
        
        return ResponseEntity.ok(progress); // Return 200 OK with the list of content progress
    }
}
