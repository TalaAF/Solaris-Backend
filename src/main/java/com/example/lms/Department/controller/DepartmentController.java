package com.example.lms.Department.controller;

import com.example.lms.Department.dto.DepartmentDTO;
import com.example.lms.Department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "APIs for managing academic departments")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;
    
    @GetMapping
    @Operation(summary = "Get all departments", description = "Retrieves list of all departments with optional active-only filter")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved departments list")
    })
    public ResponseEntity<List<DepartmentDTO.Response>> getAllDepartments(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        List<DepartmentDTO.Response> departments;
        if (activeOnly) {
            departments = departmentService.getAllActiveDepartments();
        } else {
            departments = departmentService.getAllDepartments();
        }
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Retrieves department details by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department found"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentDTO.Response> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }
    
    @PostMapping
    @Operation(summary = "Create department", description = "Creates a new academic department")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Department created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<DepartmentDTO.Response> createDepartment(
            @Valid @RequestBody DepartmentDTO.Request request) {
        return new ResponseEntity<>(departmentService.createDepartment(request), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Updates an existing department's information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<DepartmentDTO.Response> updateDepartment(
            @PathVariable Long id, 
            @Valid @RequestBody DepartmentDTO.Request request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Deletes a department by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/pageable")
    @Operation(summary = "Get paginated departments", description = "Retrieves paginated list of departments with optional active-only filter")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated departments")
    })
    public ResponseEntity<Page<DepartmentDTO.Response>> getPaginatedDepartments(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            Pageable pageable) {
        Page<DepartmentDTO.Response> departments;
        if (activeOnly) {
            departments = departmentService.getAllActiveDepartmentsPageable(pageable);
        } else {
            departments = departmentService.getAllDepartmentsPageable(pageable);
        }
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search departments", description = "Search departments by name, code, or specialty area")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    })
    public ResponseEntity<Page<DepartmentDTO.Response>> searchDepartments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            Pageable pageable) {
        return ResponseEntity.ok(departmentService.searchDepartments(keyword, activeOnly, pageable));
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle department status", description = "Activate or deactivate a department")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Department status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    public ResponseEntity<DepartmentDTO.Response> toggleDepartmentStatus(
            @PathVariable Long id, 
            @RequestParam boolean active) {
        return ResponseEntity.ok(departmentService.updateDepartmentStatus(id, active));
    }
    
    @GetMapping("/counts")
    @Operation(summary = "Get department counts", description = "Get count statistics of departments")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved department counts")
    })
    public ResponseEntity<Map<String, Long>> getDepartmentCounts() {
        return ResponseEntity.ok(departmentService.getDepartmentCounts());
    }
    
    @PostMapping("/batch")
    @Operation(summary = "Batch create departments", description = "Create multiple departments at once")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Departments created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<List<DepartmentDTO.Response>> batchCreateDepartments(
            @Valid @RequestBody List<DepartmentDTO.Request> requests) {
        return new ResponseEntity<>(departmentService.batchCreateDepartments(requests), HttpStatus.CREATED);
    }
    
    @PatchMapping("/{id}/head")
    @Operation(summary = "Assign department head", description = "Assign a user as head of department")
    public ResponseEntity<DepartmentDTO.Response> assignDepartmentHead(
            @PathVariable Long id, 
            @RequestBody Map<String, Long> request) {
        
        Long userId = request.get("userId");
        return ResponseEntity.ok(departmentService.assignDepartmentHead(id, userId));
    }

    @DeleteMapping("/{id}/head")
    @Operation(summary = "Remove department head", description = "Remove the current head of department")
    public ResponseEntity<DepartmentDTO.Response> removeDepartmentHead(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.removeDepartmentHead(id));
    }
}