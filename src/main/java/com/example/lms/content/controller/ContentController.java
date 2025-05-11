package com.example.lms.content.controller;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.service.ContentService;
import com.example.lms.course.dto.CourseDTO;
import com.example.lms.user.model.User;

import lombok.RequiredArgsConstructor;
import com.example.lms.content.model.Content;
import com.example.lms.content.model.ContentVersion;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.lms.content.service.ContentFileStorageService;


@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "Content", description = "APIs for managing course contents")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class ContentController {

    private final ContentService contentService;
    private final ContentFileStorageService fileStorageService; // Add this line

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Create new content", description = "Creates new content for a course")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<?> createContent(
            @RequestParam Long courseId,
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description) {
        
        if (file.isEmpty() || title.isBlank()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "File and title are required.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Content content = contentService.createContent(courseId, file, title, description);
            ContentDTO contentDTO = contentService.convertToDTO(content);
            return ResponseEntity.ok(contentDTO);
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get content by ID", description = "Retrieves content by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<ContentDTO> getContent(@PathVariable Long id) {
        Content content = contentService.getContentById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));
        
        // Log access
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            contentService.logContentAccess(id, user.getId());
        }
        
        return ResponseEntity.ok(contentService.convertToDTO(content));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get contents by course", description = "Retrieves all contents for a course")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contents retrieved successfully"),
        @ApiResponse(responseCode = "204", description = "No contents found")
    })
    public ResponseEntity<CollectionModel<EntityModel<ContentDTO>>> getContentsByCourseId(@PathVariable Long courseId) {
        List<Content> contents = contentService.getContentsByCourseId(courseId);
        if (contents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<ContentDTO>> contentModels = contents.stream()
                .map(content -> {
                    ContentDTO contentDTO = contentService.convertToDTO(content);
                    EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
                    // Fix: Change to the correct method reference that exists in the controller
                    contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContent(content.getId())).withSelfRel());
                    contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(courseId)).withRel("course-contents"));
                    return contentModel;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ContentDTO>> collectionModel = CollectionModel.of(contentModels);
        collectionModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(courseId)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Update content", description = "Updates existing content")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<ContentDTO> updateContent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        
        String title = (String) updates.get("title");
        String description = (String) updates.get("description");
        Boolean isPublished = (Boolean) updates.get("isPublished");
        Integer duration = (Integer) updates.get("duration");
        
        Content updatedContent = contentService.updateContentDetails(
            id, title, description, isPublished, duration);
        
        return ResponseEntity.ok(contentService.convertToDTO(updatedContent));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Delete content", description = "Deletes content by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Content deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        boolean deleted = contentService.deleteContent(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "Get content versions", description = "Retrieves version history of content")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Versions retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<List<ContentVersion>> getContentVersions(@PathVariable Long id) {
        List<ContentVersion> versions = contentService.getContentVersions(id);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/search")
    @Operation(summary = "Search contents", description = "Searches contents by keyword")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results retrieved")
    })
    public ResponseEntity<Page<Content>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Content> result = contentService.searchByKeyword(keyword, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter contents", description = "Filters contents by tags or file type")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Filtered results retrieved")
    })
    public List<Content> filterContents(
            @RequestParam(required = false) String tags, 
            @RequestParam(required = false) String fileType) {
        return contentService.filterContents(tags, fileType);
    }

    @PostMapping("/{contentId}/mark-viewed")
    @Operation(summary = "Mark content as viewed", description = "Marks content as viewed by a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content marked as viewed"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<?> markContentAsViewed(
            @PathVariable Long contentId,
            @RequestBody Map<String, Long> requestBody) {
        
        Long userId = requestBody.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        
        contentService.logContentAccess(contentId, userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Get paginated content list
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllContents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.Direction.fromString(sortDir), sortBy);
        
        Page<ContentDTO> contentPage = contentService.getContentsList(
            keyword, fileType, tags, isPublished, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", contentPage.getContent());
        response.put("pageable", Map.of(
            "page", contentPage.getNumber(),
            "size", contentPage.getSize(),
            "sort", Collections.singletonList(Map.of(
                "property", sortBy,
                "direction", sortDir.toUpperCase()
            ))
        ));
        response.put("totalElements", contentPage.getTotalElements());
        response.put("totalPages", contentPage.getTotalPages());
        response.put("last", contentPage.isLast());
        response.put("first", contentPage.isFirst());
        
        return ResponseEntity.ok(response);
    }

    // Download content file
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadContent(@PathVariable Long id) {
        Content content = contentService.getContentById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));
        
        Resource resource = fileStorageService.loadFileAsResource(content.getFilePath());
        
        // Log access
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            contentService.logContentAccess(id, user.getId());
        }
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .contentType(MediaType.parseMediaType(content.getFileType()))
            .body(resource);
    }

    // Add these endpoints
    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore deleted content", description = "Restores content that was previously deleted")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content restored successfully"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<Void> restoreContent(@PathVariable Long id) {
        boolean restored = contentService.restoreContent(id);
        if (restored) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/deleted")
    @Operation(summary = "Get deleted content", description = "Retrieves all soft-deleted content with pagination")
    public ResponseEntity<Page<ContentDTO>> getDeletedContent(
            Pageable pageable) {
        return ResponseEntity.ok(contentService.getDeletedContents(pageable));
    }

    @PostMapping(value = "/modules/{moduleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Add content to a module", description = "Create new content and add it to a module")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Content created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Module not found")
    })
    public ResponseEntity<ContentDTO> addContentToModule(
            @PathVariable Long moduleId,
            @RequestPart("contentData") @Valid ContentDTO contentDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        ContentDTO savedContent = contentService.createContent(contentDTO, moduleId, file);
        return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ContentDTO>> getContentsByModule(@PathVariable Long moduleId) {
        try {
            // Add debugging
            log.info("Fetching contents for module: {}", moduleId);
            
            // Use the service instead of repository directly
            List<Content> contents = contentService.getContentsByModuleId(moduleId);
            List<ContentDTO> contentDTOs = contents.stream()
                .map(contentService::convertToDTO)  // Use existing conversion method
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(contentDTOs);
        } catch (Exception e) {
            log.error("Error fetching contents for module {}: {}", moduleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Add this new endpoint to handle JSON requests
    @PostMapping(value = "/modules/{moduleId}/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Add content to a module (JSON)", description = "Create new content using JSON format")
    public ResponseEntity<ContentDTO> addContentToModuleJson(
            @PathVariable Long moduleId,
            @RequestBody ContentDTO contentDTO) {
        
        log.info("Received JSON request to create content for module: {}", moduleId);
        log.debug("Content data: {}", contentDTO);
        
        try {
            ContentDTO savedContent = contentService.createContent(contentDTO, moduleId, null);
            return new ResponseEntity<>(savedContent, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating content via JSON endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}