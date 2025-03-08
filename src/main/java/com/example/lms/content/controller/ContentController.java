package com.example.lms.content.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

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
       ContentDTO contentDTO = contentService.convertToDTO(content);

       EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
       contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(content.getId(), null)).withSelfRel()); // Pass null for userId
       contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentsByCourseId(courseId)).withRel("course-contents"));

       return ResponseEntity.ok(contentModel);
   }
   
    // Get content by ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ContentDTO>> getContentById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) { // Make userId optional

        if (userId != null) {
            contentService.logContentAccess(id, userId); // // Check in if userId is provided
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

    // Get all content for a given course
   @GetMapping("/course/{courseId}")
   public ResponseEntity<CollectionModel<EntityModel<ContentDTO>>> getContentsByCourseId(@PathVariable Long courseId) {
       List<Content> contents = contentService.getContentsByCourseId(courseId);
       if (contents.isEmpty()) {
           return ResponseEntity.noContent().build();
       }

       List<EntityModel<ContentDTO>> contentModels = contents.stream()
               .map(content -> {
                   ContentDTO contentDTO = contentService.convertToDTO(content);
                   EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
                   contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(content.getId(), null)).withSelfRel()); // Pass null for userId
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
             ContentDTO contentDTO = contentService.convertToDTO(updatedContent.get());
 
             EntityModel<ContentDTO> contentModel = EntityModel.of(contentDTO);
             contentModel.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ContentController.class).getContentById(id, null)).withSelfRel()); // Pass null for userId
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

    // Get content versions
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<ContentVersion>> getContentVersions(@PathVariable Long id) {
        List<ContentVersion> versions = contentService.getContentVersions(id);
        return ResponseEntity.ok(versions);
    }

    
  // Search content by keyword
  @GetMapping("/search")
  public ResponseEntity<Page<Content>> search(
          @RequestParam String keyword,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
      Pageable pageable = PageRequest.of(page, size);
      Page<Content> result = contentService.searchByKeyword(keyword, pageable);
      return ResponseEntity.ok(result);
  }
  
}