package com.example.lms.content.controller;

import com.example.lms.content.dto.ModuleDTO;
import com.example.lms.content.dto.ModuleOrderRequest;
import com.example.lms.content.service.ModuleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/modules")
@Tag(name = "Module Management", description = "APIs for managing course modules")
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get modules by course", description = "Retrieve all modules for a specific course")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Modules retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<List<ModuleDTO>> getModulesByCourse(@PathVariable Long courseId) {
        List<ModuleDTO> modules = moduleService.getModulesByCourse(courseId);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get module by ID", description = "Retrieve a specific module by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Module retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Module not found")
    })
    public ResponseEntity<ModuleDTO> getModuleById(@PathVariable Long id) {
        ModuleDTO module = moduleService.getModuleById(id);
        return ResponseEntity.ok(module);
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Create module", description = "Create a new module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Module created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ModuleDTO> createModule(@Valid @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO createdModule = moduleService.createModule(moduleDTO);
        return ResponseEntity.ok(createdModule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Update module", description = "Update an existing module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Module updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Module not found")
    })
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable Long id, @Valid @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO updatedModule = moduleService.updateModule(id, moduleDTO);
        return ResponseEntity.ok(updatedModule);
    }

    @PostMapping("/reorder")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Reorder modules", description = "Change the sequence of multiple modules")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Modules reordered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<ModuleDTO>> reorderModules(@RequestBody List<ModuleOrderRequest> reorderRequests) {
        List<ModuleDTO> reorderedModules = moduleService.reorderModules(reorderRequests);
        return ResponseEntity.ok(reorderedModules);
    }

    @GetMapping("/{moduleId}/contents-order")
    @Operation(summary = "Get content order", description = "Get the order of contents within a module")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Content order retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Module not found")
    })
    public ResponseEntity<List<Object>> getContentsOrder(@PathVariable Long moduleId) {
        List<Object> contentOrder = moduleService.getContentsOrder(moduleId);
        return ResponseEntity.ok(contentOrder);
    }
}