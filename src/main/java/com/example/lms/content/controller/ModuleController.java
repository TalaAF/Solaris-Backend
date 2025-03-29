package com.example.lms.content.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lms.content.model.Content;
import com.example.lms.content.model.Module;
import com.example.lms.content.model.Tag;
import com.example.lms.content.service.ContentService;
import com.example.lms.content.service.ModuleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tags;

@RestController
@RequestMapping("/api/modules")
@Tags(@io.swagger.v3.oas.annotations.tags.Tag(name = "Module Management", description = "APIs for managing course modules and content organization"))
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;
    
    @Autowired
    private ContentService contentService;

    @GetMapping
    @Operation(summary = "Get all modules", description = "Retrieves a list of all course modules")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved modules")
    })
    public ResponseEntity<List<Module>> getAllModules() {
        List<Module> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @PostMapping
    @Operation(summary = "Create module", description = "Creates a new course module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Module created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid module data")
    })
    public ResponseEntity<Module> createModule(@RequestBody Module module) {
        Module createdModule = moduleService.createModule(module);
        return ResponseEntity.ok(createdModule);
    }

    @PostMapping("/reorder")
    @Operation(summary = "Reorder modules", description = "Changes the order of modules based on provided IDs list")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Modules reordered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid module IDs list")
    })
    public ResponseEntity<Void> reorderModules(@RequestBody List<Long> moduleIds) {
        moduleService.reorderModules(moduleIds);
        return ResponseEntity.ok().build(); 
    }

    @PostMapping("/{contentId}/assign-to-module/{moduleId}")
    @Operation(summary = "Assign content to module", description = "Links a content item to a specific module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content assigned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid content or module ID"),
        @ApiResponse(responseCode = "404", description = "Content or module not found")
    })
    public ResponseEntity<Content> assignContentToModule(
            @PathVariable Long contentId,
            @PathVariable Long moduleId) {
        Content content = contentService.assignContentToModule(contentId, moduleId);
        return ResponseEntity.ok(content);
    }

    @PostMapping("/{moduleId}/reorder-contents")
    @Operation(summary = "Reorder module contents", description = "Changes the order of contents within a module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contents reordered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid content IDs list")
    })
    public ResponseEntity<Void> reorderContents(
            @PathVariable Long moduleId,
            @RequestBody List<Long> contentIds) {
        moduleService.reorderContents(moduleId, contentIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{contentId}/add-tag")
    @Operation(summary = "Add tag to content", description = "Associates a tag with a content item")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid tag data"),
        @ApiResponse(responseCode = "404", description = "Content not found")
    })
    public ResponseEntity<Content> addTagToContent(
            @PathVariable Long contentId,
            @RequestBody Tag tag) {
        Content content = contentService.addTagToContent(contentId, tag);
        return ResponseEntity.ok(content);
    }

    @PostMapping("/{moduleId}/validate-sequence")
    @Operation(summary = "Validate content sequence", description = "Validates the order of contents in a module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sequence is valid"),
        @ApiResponse(responseCode = "400", description = "Sequence is invalid")
    })
    public ResponseEntity<String> validateSequence(@PathVariable Long moduleId, @RequestBody List<Long> contentIds) {
        boolean isValid = moduleService.validateContentSequence(moduleId, contentIds);

        if (isValid) {
            return ResponseEntity.ok("The sequence is correct.");
        } else {
            return ResponseEntity.badRequest().body("The sequence is incorrect.");
        }
    }

    @GetMapping("/{moduleId}/contents-order")
    @Operation(summary = "Get contents order", description = "Retrieves the current order of contents in a module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved contents order"),
        @ApiResponse(responseCode = "404", description = "Module not found")
    })
    public ResponseEntity<List<Content>> getContentsOrder(@PathVariable Long moduleId) {
        List<Content> contents = moduleService.getContentsOrder(moduleId);
        return ResponseEntity.ok(contents);
    }
}