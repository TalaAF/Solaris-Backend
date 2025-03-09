package com.example.lms.Department.controller;

import com.example.lms.Department.dto.DepartmentDTO;
import com.example.lms.Department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    
    @GetMapping
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
    public ResponseEntity<DepartmentDTO.Response> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }
    
    @PostMapping
    
    public ResponseEntity<DepartmentDTO.Response> createDepartment(
            @Valid @RequestBody DepartmentDTO.Request request) {
        return new ResponseEntity<>(departmentService.createDepartment(request), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    
    public ResponseEntity<DepartmentDTO.Response> updateDepartment(
            @PathVariable Long id, 
            @Valid @RequestBody DepartmentDTO.Request request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }
    
    @DeleteMapping("/{id}")
    
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}