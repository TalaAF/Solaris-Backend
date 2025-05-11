package com.example.lms.content.service;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.mapper.ContentMapper;
import com.example.lms.content.model.Content;
import com.example.lms.content.model.ContentAccessLog;
import com.example.lms.content.model.ContentType;
import com.example.lms.content.model.ContentVersion;
import com.example.lms.content.model.Module;
import com.example.lms.content.model.Tag;
import com.example.lms.content.repository.ContentAccessLogRepository;
import com.example.lms.content.repository.ContentRepository;
import com.example.lms.content.repository.ContentVersionRepository;
import com.example.lms.content.repository.ModuleRepository;
import com.example.lms.content.repository.TagRepository;
import com.example.lms.course.dto.CourseDTO;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ContentMetadataExtractor metadataExtractor;

    @Autowired
    private ContentFileStorageService fileStorageService;

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

    // Enhanced create content method
    @Transactional
    public Content createContent(Long courseId, MultipartFile file, String title, String description, 
                                boolean isPublished, Integer duration) {
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
        content.setIsPublished(isPublished);
        content.setDuration(duration);
        
        // Save content to database
        return contentRepository.save(content);
    }

    // Get content by ID
    @Transactional(readOnly = true)
    public Optional<Content> getContentById(Long id) {
        return contentRepository.findById(id);
    }

    // Get all content for a given course
    public List<Content> getContentsByCourseId(Long courseId) {
        return contentRepository.findByCourseId(courseId);
    }


    // Update content
    @Transactional
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
    @Transactional
    public boolean deleteContent(Long id) {
        return contentRepository.findById(id).map(content -> {
            // Instead of removing from database, mark as deleted
            content.setDeleted(true);
            contentRepository.save(content);
            return true;
        }).orElse(false);
    }

    // Add method to permanently delete content if needed
    @Transactional
    public boolean permanentlyDeleteContent(Long id) {
        return contentRepository.findById(id).map(content -> {
            // Delete the associated file from storage
            try {
                fileStorageService.deleteFile(content.getFilePath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Delete content from database
            contentRepository.delete(content);
            return true;
        }).orElse(false);
    }


    
    // Convert Content to ContentDTO - modified to use only available fields
    public ContentDTO convertToDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setDescription(content.getDescription());
        dto.setFilePath(content.getFilePath());
        // Remove or comment out unsupported fields
        // dto.setFileType(content.getFileType());
        // dto.setFileSize(content.getFileSize());
        // dto.setCreatedAt(content.getCreatedAt().toString());
        // dto.setUpdatedAt(content.getUpdatedAt().toString());
        
        if (content.getCourse() != null) {
            dto.setCourseId(content.getCourse().getId());
            // dto.setCourseName(content.getCourse().getTitle());
        }
        
        if (content.getModule() != null) {
            dto.setModuleId(content.getModule().getId());
            // dto.setModuleName(content.getModule().getTitle());
        }
        
        dto.setOrder(content.getOrder());
        dto.setType(content.getType() != null ? content.getType().name() : null);
        dto.setContent(content.getContent());
        dto.setVideoUrl(content.getVideoUrl());
        dto.setIsPublished(content.isPublished());
        dto.setDuration(content.getDuration());
        
        return dto;
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

   @Transactional
public Content assignContentToModule(Long contentId, Long moduleId) {
    Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));
            
            com.example.lms.content.model.Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
    
    // Determine the next order value for this content in the module
    Integer maxOrder = content.getModule() == null ? 0 : 
        module.getContents().stream()
            .map(Content::getOrder)
            .filter(Objects::nonNull)
            .max(Integer::compareTo)
            .orElse(0);
            
    content.setModule(module);
    content.setOrder(maxOrder + 1);
    
    return contentRepository.save(content);
}

    
@Transactional(readOnly = true)
public Page<Content> searchByKeyword(String keyword, Pageable pageable) {
    if (keyword == null || keyword.trim().isEmpty()) {
        return contentRepository.findAll(pageable);
    }
    
    String searchTerm = "%" + keyword.toLowerCase() + "%";
    return contentRepository.findByTitleContainingOrDescriptionContaining(searchTerm, searchTerm, pageable);
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

    @Transactional
    public Content addTagToContent(Long contentId, Tag tag) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));
                
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }
        
        // Normalize tag name - lowercase and trim
        String normalizedTagName = tag.getName().toLowerCase().trim();
        tag.setName(normalizedTagName);
        
        // Find existing tag or create new one
        Tag existingTag = tagRepository.findByName(normalizedTagName);
        Tag tagToUse = existingTag != null ? existingTag : tagRepository.save(tag);
        
        // Check if content already has this tag
        boolean tagExists = content.getTags().stream()
                .anyMatch(t -> t.getName().equals(normalizedTagName));
                
        if (!tagExists) {
            content.getTags().add(tagToUse);
            return contentRepository.save(content);
        }
        
        return content;
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

    // Add toggle publication status method
    @Transactional
    public Content togglePublicationStatus(Long contentId, boolean isPublished) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));
        content.setIsPublished(isPublished);
        return contentRepository.save(content);
    }

    // Enhanced content search with pagination
    public Page<ContentDTO> getContentsList(String keyword, String fileType, 
                                           List<String> tags, Boolean isPublished,
                                           Pageable pageable) {
        Page<Content> contentPage;
        
        // Implement logic to filter by multiple criteria
        if (keyword != null && !keyword.isEmpty()) {
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            contentPage = contentRepository.findBySearchCriteria(searchTerm, fileType, tags, isPublished, pageable);
        } else if (fileType != null || (tags != null && !tags.isEmpty()) || isPublished != null) {
            contentPage = contentRepository.findByFilters(fileType, tags, isPublished, pageable);
        } else {
            contentPage = contentRepository.findAll(pageable);
        }
        
        return contentPage.map(this::convertToDTO);
    }

    @Transactional
public Content updateContentDetails(Long id, String title, String description, Boolean isPublished, Integer duration) {
    return contentRepository.findById(id)
        .map(content -> {
            // Save the current version before updating
            ContentVersion version = new ContentVersion();
            version.setTitle(content.getTitle());
            version.setDescription(content.getDescription());
            version.setFilePath(content.getFilePath());
            version.setFileType(content.getFileType());
            version.setFileSize(content.getFileSize());
            version.setContent(content);
            contentVersionRepository.save(version);

            // Update the content with new values
            if (title != null) content.setTitle(title);
            if (description != null) content.setDescription(description);
            if (isPublished != null) content.setIsPublished(isPublished);
            if (duration != null) content.setDuration(duration);
            
            return contentRepository.save(content);
        })
        .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));
}

    // Add this method to ContentService
    @Transactional
    public boolean restoreContent(Long id) {
        try {
            contentRepository.restoreContent(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Add method to get deleted content
    @Transactional(readOnly = true)
    public Page<ContentDTO> getDeletedContents(Pageable pageable) {
        return contentRepository.findDeleted(pageable).map(this::convertToDTO);
    }

    /**
     * Create content from form submission
     * 
     * @param contentDTO Data from frontend form
     * @param moduleId The module to add content to
     * @param file Optional file upload
     * @return Created content
     */
    @Transactional
    public ContentDTO createContent(ContentDTO contentDTO, Long moduleId, MultipartFile file) {
        try {
            log.info("Creating content with data: {}, moduleId: {}", contentDTO, moduleId);
            
            Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
            
            Content content = new Content();
            content.setTitle(contentDTO.getTitle());
            content.setDescription(contentDTO.getDescription());
            content.setDuration(contentDTO.getDuration());
            content.setOrder(contentDTO.getOrder() != null ? contentDTO.getOrder() : 0);
            
            // Set default values for required fields to avoid constraint violations
            content.setFilePath("DEFAULT_PATH");
            
            // Fix type handling to match database expectations
            try {
                if (contentDTO.getType() != null) {
                    // Convert to uppercase and use enum
                    String typeName = contentDTO.getType().trim().toUpperCase();
                    
                    // Special case handling - try to match nearest enum value
                    if (typeName.equals("DOCUMENT") || typeName.equals("DOC")) {
                        content.setType(ContentType.DOCUMENT);
                    } else if (typeName.equals("VIDEO") || typeName.equals("VID")) {
                        content.setType(ContentType.VIDEO);
                    } else if (typeName.equals("QUIZ") || typeName.equals("TEST")) {
                        content.setType(ContentType.QUIZ);
                    } else {
                        // Try direct mapping to enum
                        try {
                            content.setType(ContentType.valueOf(typeName));
                        } catch (IllegalArgumentException e) {
                            // If all else fails, use a default safe value
                            log.warn("Unknown content type: '{}', defaulting to ARTICLE", typeName);
                            content.setType(ContentType.ARTICLE);
                        }
                    }
                    
                    // Log the final type being used
                    log.info("Setting content type to: {}", content.getType());
                } else {
                    // Default type
                    content.setType(ContentType.ARTICLE);
                    log.info("No type specified, defaulting to ARTICLE");
                }
            } catch (Exception e) {
                log.error("Error setting content type: {}", e.getMessage());
                // Safe fallback
                content.setType(ContentType.ARTICLE);
            }
            
            content.setModule(module);
            content.setCourse(module.getCourse());
            content.setIsPublished(false);
            
            // Handle different content types with proper null handling
            switch (content.getType()) {
                case DOCUMENT:
                    if (file != null && !file.isEmpty()) {
                        String filePath = fileStorageService.storeFile(file);
                        content.setFilePath(filePath);
                        log.info("Stored file at: {}", filePath);
                    } else {
                        content.setContent(contentDTO.getContent() != null ? 
                                      contentDTO.getContent() : "");
                    }
                    break;
                    
                case VIDEO:
                    content.setVideoUrl(contentDTO.getVideoUrl());
                    break;
                    
                case QUIZ:
                    content.setContent(contentDTO.getContent() != null ? 
                                  contentDTO.getContent() : "{}");
                    break;
                    
                default:
                    // Handle other types
                    break;
            }
            
            log.info("About to save content with type: {}", content.getType());
            Content savedContent = contentRepository.save(content);
            log.info("Content saved successfully with ID: {}", savedContent.getId());
            
            return ContentMapper.toDTO(savedContent);
        } catch (Exception e) {
            log.error("Error creating content: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Add this method to handle getContentsByModuleId which was missing
    @Transactional(readOnly = true)
    public List<Content> getContentsByModuleId(Long moduleId) {
        log.info("Getting contents for module ID: {}", moduleId);
        
        // First check if module exists
        moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        // Get contents for this module, using a query method that matches your entity relationship
        List<Content> contents;
        try {
            // Use a direct query to avoid any JPA mapping issues
            contents = contentRepository.findByModuleId(moduleId);
            log.info("Found {} content items for module {}", contents.size(), moduleId);
        } catch (Exception e) {
            log.error("Error finding content by module ID: {}", e.getMessage());
            throw e;
        }
        
        return contents;
    }
}
