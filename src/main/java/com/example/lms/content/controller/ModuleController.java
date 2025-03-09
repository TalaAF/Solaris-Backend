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

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;
    @Autowired
private ContentService contentService;

    @GetMapping
    public ResponseEntity<List<Module>> getAllModules() {
        List<Module> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @PostMapping
    public ResponseEntity<Module> createModule(@RequestBody Module module) {
        Module createdModule = moduleService.createModule(module);
        return ResponseEntity.ok(createdModule);
    }

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderModules(@RequestBody List<Long> moduleIds) {
        moduleService.reorderModules(moduleIds);
        return ResponseEntity.ok().build(); 
    }

    @PostMapping("/{contentId}/assign-to-module/{moduleId}")
public ResponseEntity<Content> assignContentToModule(
        @PathVariable Long contentId,
        @PathVariable Long moduleId) {
    Content content = contentService.assignContentToModule(contentId, moduleId);
    return ResponseEntity.ok(content);
}
@PostMapping("/{moduleId}/reorder-contents")
public ResponseEntity<Void> reorderContents(
        @PathVariable Long moduleId,
        @RequestBody List<Long> contentIds) {
    moduleService.reorderContents(moduleId, contentIds);
    return ResponseEntity.ok().build();
}
@PostMapping("/{contentId}/add-tag")
public ResponseEntity<Content> addTagToContent(
        @PathVariable Long contentId,
        @RequestBody Tag tag) {
    Content content = contentService.addTagToContent(contentId, tag);
    return ResponseEntity.ok(content);
}

@PostMapping("/{moduleId}/validate-sequence")
public ResponseEntity<String> validateSequence(@PathVariable Long moduleId, @RequestBody List<Long> contentIds) {
    boolean isValid = moduleService.validateContentSequence(moduleId, contentIds);

    if (isValid) {
        return ResponseEntity.ok("The sequence is correct.");
    } else {
        return ResponseEntity.badRequest().body("The sequence is incorrect.");
    }
}



}