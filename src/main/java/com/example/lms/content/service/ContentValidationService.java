package com.example.lms.content.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContentValidationService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf", 
            "video/mp4",
            "image/jpeg",
            "image/png",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    
    public void validateContent(MultipartFile file, String title, String description) {
        validateFile(file);
        validateTitle(title);
        validateDescription(description);
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size (50MB)");
        }
        
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type: " + file.getContentType());
        }
    }
    
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        
        if (title.length() > 255) {
            throw new IllegalArgumentException("Title is too long (maximum 255 characters)");
        }
    }
    
    private void validateDescription(String description) {
        if (description != null && description.length() > 5000) {
            throw new IllegalArgumentException("Description is too long (maximum 5000 characters)");
        }
    }
}