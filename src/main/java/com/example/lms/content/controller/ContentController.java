package com.example.lms.content.controller;

import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.service.ContentService;
import com.example.lms.course.dto.CourseDTO;

import lombok.RequiredArgsConstructor;

import com.example.lms.content.model.Content;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

// Convert Content to ContentDTO
private ContentDTO convertToDTO(Content content) {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(content.getCourse().getId());
        courseDTO.setTitle(content.getCourse().getTitle());
        courseDTO.setDescription(content.getCourse().getDescription());
        courseDTO.setInstructorEmail(content.getCourse().getInstructor().getEmail());
        courseDTO.setDepartmentId(content.getCourse().getDepartment().getId());
        courseDTO.setDepartmentName(content.getCourse().getDepartment().getName());

        return new ContentDTO(
                content.getId(),
                content.getTitle(),
                content.getDescription(),
                content.getFilePath(),
                content.getFileType(),
                content.getFileSize(),
                content.getCreatedAt().toString(),
                content.getUpdatedAt().toString(),
                courseDTO
        );
    }

    // Create new content
    @PostMapping
    public ResponseEntity<?> createContent(
            @RequestParam Long courseId,
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description) {

        if (file.isEmpty() || title.isBlank()) {
            return ResponseEntity.badRequest().body("File and title are required.");
        }

        Content content = contentService.createContent(courseId, file, title, description);
        ContentDTO contentDTO = convertToDTO(content);

        EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
        contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(content.getId())).withSelfRel());
        contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(courseId)).withRel("course-contents"));

        return ResponseEntity.ok(contentModel);
    }

    // Get content by ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ContentDTO>> getContentById(@PathVariable Long id) {
        Optional<Content> content = contentService.getContentById(id);
        if (content.isPresent()) {
            ContentDTO contentDTO = convertToDTO(content.get());

            EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(id)).withSelfRel());
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(content.get().getCourse().getId())).withRel("course-contents"));
            return ResponseEntity.ok(contentModel);
        }
        return ResponseEntity.notFound().build();
    }

    // Get all content for a given course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<CollectionModel<EntityModel<ContentDTO>>> getContentsByCourseId(@PathVariable Long courseId) {
        List<Content> contents = contentService.getContentsByCourseId(courseId);
        if (contents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<ContentDTO>> contentModels = contents.stream()
                .map(content -> {
                    ContentDTO contentDTO = convertToDTO(content);
                    EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
                    contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(content.getId())).withSelfRel());
                    contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(courseId)).withRel("course-contents"));
                    return contentModel;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ContentDTO>> collectionModel = CollectionModel.of(contentModels);
        collectionModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(courseId)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    // Update existing content
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContent(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) {

        Optional<Content> updatedContent = contentService.updateContent(id, title, description);
        if (updatedContent.isPresent()) {
            ContentDTO contentDTO = convertToDTO(updatedContent.get());

            EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(id)).withSelfRel());
            contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(updatedContent.get().getCourse().getId())).withRel("course-contents"));
            return ResponseEntity.ok(contentModel);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        if (contentService.deleteContent(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
