package com.example.lms.Department.service;

import com.example.lms.Department.dto.DepartmentDTO;

import java.util.List;

public interface DepartmentService {
    
    List<DepartmentDTO.Response> getAllDepartments();
    
    List<DepartmentDTO.Response> getAllActiveDepartments();
    
    DepartmentDTO.Response getDepartmentById(Long id);
    
    DepartmentDTO.Response createDepartment(DepartmentDTO.Request request);
    
    DepartmentDTO.Response updateDepartment(Long id, DepartmentDTO.Request request);
    
    void deleteDepartment(Long id);
}
