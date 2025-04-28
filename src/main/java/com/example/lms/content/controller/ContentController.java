package com.example.lms.content.controller;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.service.ContentService;
import com.example.lms.course.dto.CourseDTO;
import lombok.RequiredArgsConstructor;
import com.example.lms.content.model.Content;
import com.example.lms.content.model.ContentVersion;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "Content", description = "APIs for managing course contents")
@SecurityRequirement(name = "bearerAuth")
public class ContentController {

    private final ContentService contentService;

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
    public ResponseEntity<EntityModel<ContentDTO>> getContentById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {

        if (userId != null) {
            contentService.logContentAccess(id, userId);
        }
    
        Optional<Content> content = contentService.getContentById(id);
        if (content.isPresent()) {
            ContentDTO contentDTO = contentService.convertToDTO(content.get());
            EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(id, userId)).withSelfRel());
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(content.get().getCourse().getId())).withRel("course-contents"));
            return ResponseEntity.ok(contentModel);
        }
        return ResponseEntity.notFound().build();
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
                    contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(content.getId(), null)).withSelfRel());
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
    public ResponseEntity<?> updateContent(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) {

        Optional<Content> updatedContent = contentService.updateContent(id, title, description);
        if (updatedContent.isPresent()) {
            ContentDTO contentDTO = contentService.convertToDTO(updatedContent.get());
            EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(id, null)).withSelfRel());
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(updatedContent.get().getCourse().getId())).withRel("course-contents"));
            return ResponseEntity.ok(contentModel);
        }
        return ResponseEntity.notFound().build();
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
        if (contentService.deleteContent(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
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
}