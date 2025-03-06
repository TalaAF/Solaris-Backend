package com.example.lms.content.controller;

import com.example.lms.content.model.Content;
import com.example.lms.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    @Autowired
    private ContentService contentService;

// Create new content
@PostMapping
    public ResponseEntity<Content> createContent(
            @RequestParam Long courseId,
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description) {
        Content content = contentService.createContent(courseId, file, title, description);
        return ResponseEntity.ok(content);
    }

// Get content by ID
@GetMapping("/{id}")
    public ResponseEntity<Content> getContentById(@PathVariable Long id) {
        Content content = contentService.getContentById(id);
        return ResponseEntity.ok(content);
    }

// Get all content for a given course
@GetMapping("/course/{courseId}")
    public ResponseEntity<List<Content>> getContentsByCourseId(@PathVariable Long courseId) {
        List<Content> contents = contentService.getContentsByCourseId(courseId);
        return ResponseEntity.ok(contents);
    }

// Update existing content
@PutMapping("/{id}")
    public ResponseEntity<Content> updateContent(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) {
        Content content = contentService.updateContent(id, title, description);
        return ResponseEntity.ok(content);
    }

// Delete content
@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}