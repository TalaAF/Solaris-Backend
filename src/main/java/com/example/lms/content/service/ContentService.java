package com.example.lms.content.service;

import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.model.Content;
import com.example.lms.content.model.ContentAccessLog;
import com.example.lms.content.model.ContentVersion;
import com.example.lms.content.model.Tag;
import com.example.lms.content.repository.ContentAccessLogRepository;
import com.example.lms.content.repository.ContentRepository;
import com.example.lms.content.repository.ContentVersionRepository;
import com.example.lms.content.repository.ModuleRepository;
import com.example.lms.content.repository.TagRepository;
import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    @Autowired
    private ContentVersionRepository contentVersionRepository;

    @Autowired
    private ContentAccessLogRepository contentAccessLogRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
     private TagRepository tagRepository;
    
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


    // Update content
    public Optional<Content> updateContent(Long id, String title, String description) {
        return contentRepository.findById(id).map(content -> {
            // Save the current version as a previous version
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

    // Get all versions of a content
    public List<ContentVersion> getContentVersions(Long contentId) {
        return contentVersionRepository.findByContentId(contentId);
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


    
    // Convert Content to ContentDTO
    public ContentDTO convertToDTO(Content content) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(content.getCourse().getId());
        courseDTO.setTitle(content.getCourse().getTitle());
        courseDTO.setDescription(content.getCourse().getDescription());
        courseDTO.setInstructorEmail(content.getCourse().getInstructor().getEmail());
        courseDTO.setDepartmentId(content.getCourse().getDepartment().getId());
        courseDTO.setDepartmentName(content.getCourse().getDepartment().getName());

        // Generate preview
        String preview = generatePreview(content.getDescription(), 100);

        return new ContentDTO(
                content.getId(),
                content.getTitle(),
                content.getDescription(),
                content.getFilePath(),
                content.getFileType(),
                content.getFileSize(),
                content.getCreatedAt().toString(),
                content.getUpdatedAt().toString(),
                preview, // Add preview
                courseDTO
        );
    }

    // Generate preview
    private String generatePreview(String content, int previewLength) {
        if (content == null || content.length() <= previewLength) {
            return content;
        }
        return content.substring(0, previewLength) + "...";
    }

// Log content access
    public void logContentAccess(Long contentId, Long userId) {
        ContentAccessLog log = new ContentAccessLog();
        log.setContentId(contentId);
        log.setUserId(userId);
        log.setAccessedAt(LocalDateTime.now());
        contentAccessLogRepository.save(log);
    }

    public Content assignContentToModule(Long contentId, Long moduleId) {
        return contentRepository.findById(contentId).map(content -> {
            moduleRepository.findById(moduleId).ifPresent(content::setModule);
            return contentRepository.save(content);
        }).orElseThrow(() -> new RuntimeException("Content not found"));
    }

    
    public Page<Content> searchByKeyword(String keyword, Pageable pageable) {
        return contentRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    public List<Content> searchByKeywordWithRelevance(String keyword) {
        List<Content> contents = contentRepository.searchByKeyword(keyword);
        contents.sort((c1, c2) -> {
            int score1 = calculateRelevanceScore(c1, keyword);
            int score2 = calculateRelevanceScore(c2, keyword);
            return Integer.compare(score2, score1); // Sort by relevance (descending)
        });
        return contents;
    }
    
    private int calculateRelevanceScore(Content content, String keyword) {
        int score = 0;
        if (content.getTitle().contains(keyword)) score += 10;
        if (content.getDescription().contains(keyword)) score += 5;
        return score;
    }

    public Content addTagToContent(Long contentId, Tag tag) {
        return contentRepository.findById(contentId).map(content -> {
            Tag existingTag = tagRepository.findByName(tag.getName());
            Tag tagToUse;
            
            if (existingTag == null) {
                tagToUse = tagRepository.save(tag);
            } else {
                tagToUse = existingTag; 
            }
    
            content.getTags().add(tagToUse);
            
            return contentRepository.save(content);
        }).orElseThrow(() -> new RuntimeException("Content not found"));
    }
    
    public List<Content> filterContents(String tags, String fileType) {
        if (tags != null && fileType != null) {
            List<String> tagsList = Arrays.asList(tags.split(","));
            return contentRepository.findByTagsAndFileType(tagsList, fileType);
        } else if (tags != null) {
            List<String> tagsList = Arrays.asList(tags.split(","));
            return contentRepository.findByTags(tagsList);
        } else if (fileType != null) {
            return contentRepository.findByFileType(fileType);
        } else {
            return contentRepository.findAll();
        }
    }
}
