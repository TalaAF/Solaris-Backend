package com.example.lms.content.service;

import com.example.lms.content.model.Content;
import com.example.lms.content.model.ContentVersion;
import com.example.lms.content.repository.ContentRepository;
import com.example.lms.content.repository.ContentVersionRepository;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ContentMetadataExtractor metadataExtractor;

    @Autowired
    private FileStorageService fileStorageService;

    private ContentVersionRepository contentVersionRepository;

    // Create new content
    public Content createContent(Long courseId, MultipartFile file, String title, String description) {
        // Check if the course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Upload the file and get its path
        String filePath = fileStorageService.storeFile(file);

        // Extract metadata
        String fileType = metadataExtractor.extractFileType(file);
        long fileSize = metadataExtractor.extractFileSize(file);

        // Create the content object
        Content content = new Content();
        content.setTitle(title);
        content.setDescription(description);
        content.setFilePath(filePath);
        content.setFileType(fileType);
        content.setFileSize(fileSize);
        content.setCourse(course);

        // Save content to database
        return contentRepository.save(content);
    }

    // Get content by ID
    public Optional<Content> getContentById(Long id) {
        return contentRepository.findById(id);
    }

    // Get all content for a given course
    public List<Content> getContentsByCourseId(Long courseId) {
        return contentRepository.findByCourseId(courseId);
    }

    // ContentService.java (Update)
public Optional<Content> updateContent(Long id, String title, String description) {
    return contentRepository.findById(id).map(content -> {
        // Save current content as a version
        ContentVersion version = new ContentVersion();
        version.setTitle(content.getTitle());
        version.setDescription(content.getDescription());
        version.setFilePath(content.getFilePath());
        version.setFileType(content.getFileType());
        version.setFileSize(content.getFileSize());
        version.setContent(content);
        contentVersionRepository.save(version);

        // Update the content
        if (title != null) content.setTitle(title);
        if (description != null) content.setDescription(description);
        return contentRepository.save(content);
    });
}

    // Delete content
    public boolean deleteContent(Long id) {
        return contentRepository.findById(id).map(content -> {
            // Delete the associated file from storage
            fileStorageService.deleteFile(content.getFilePath());
            // Delete content from database
            contentRepository.delete(content);
            return true;
        }).orElse(false);
    }
}
