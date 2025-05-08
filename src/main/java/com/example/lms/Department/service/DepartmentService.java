package com.example.lms.Department.service;

import com.example.lms.Department.dto.DepartmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    
    List<DepartmentDTO.Response> getAllDepartments();
    
    List<DepartmentDTO.Response> getAllActiveDepartments();
    
    DepartmentDTO.Response getDepartmentById(Long id);
    
    DepartmentDTO.Response createDepartment(DepartmentDTO.Request request);
    
    DepartmentDTO.Response updateDepartment(Long id, DepartmentDTO.Request request);
    
    void deleteDepartment(Long id);
    
    // New pagination methods
    Page<DepartmentDTO.Response> getAllDepartmentsPageable(Pageable pageable);
    
    Page<DepartmentDTO.Response> getAllActiveDepartmentsPageable(Pageable pageable);
    
    // New methods
    Page<DepartmentDTO.Response> searchDepartments(String keyword, boolean activeOnly, Pageable pageable);
    
    DepartmentDTO.Response updateDepartmentStatus(Long id, boolean active);
    
    Map<String, Long> getDepartmentCounts();
    
    List<DepartmentDTO.Response> batchCreateDepartments(List<DepartmentDTO.Request> requests);
    
    DepartmentDTO.Response assignDepartmentHead(Long departmentId, Long userId);
    
    DepartmentDTO.Response removeDepartmentHead(Long departmentId);
    
    // New user count methods
    Long getUserCountForDepartment(Long departmentId);
    
    Map<Long, Long> getUserCountsForAllDepartments();
    
    // Enhanced methods that include user counts
    List<DepartmentDTO.Response> getAllDepartmentsWithUserCounts();
    
    Page<DepartmentDTO.Response> getAllDepartmentsPageableWithUserCounts(Pageable pageable);
}
