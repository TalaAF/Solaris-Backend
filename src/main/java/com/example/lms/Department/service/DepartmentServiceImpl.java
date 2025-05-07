package com.example.lms.Department.service;

import com.example.lms.Department.dto.DepartmentDTO;
import com.example.lms.Department.model.Department;
import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.common.Exception.ResourceAlreadyExistsException;
import com.example.lms.common.Exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO.Response> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO.Response> getAllActiveDepartments() {
        return departmentRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO.Response getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToResponseDTO(department);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentDTO.Response> getAllDepartmentsPageable(Pageable pageable) {
        return departmentRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentDTO.Response> getAllActiveDepartmentsPageable(Pageable pageable) {
        return departmentRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponseDTO);
    }
    
    @Override
    @Transactional
    public DepartmentDTO.Response createDepartment(DepartmentDTO.Request request) {
        // Check if department with same name or code already exists
        if (departmentRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Department with name " + request.getName() + " already exists");
        }
        
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException("Department with code " + request.getCode() + " already exists");
        }
        
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setCode(request.getCode());
        department.setSpecialtyArea(request.getSpecialtyArea());
        department.setHeadOfDepartment(request.getHeadOfDepartment());
        department.setContactInformation(request.getContactInformation());
        department.setActive(request.isActive());
        
        department = departmentRepository.save(department);
        return mapToResponseDTO(department);
    }
    
    @Override
    @Transactional
    public DepartmentDTO.Response updateDepartment(Long id, DepartmentDTO.Request request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        
        // Check if updating to a name that already exists for another department
        if (!department.getName().equals(request.getName()) && 
                departmentRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Department with name " + request.getName() + " already exists");
        }
        
        // Check if updating to a code that already exists for another department
        if (!department.getCode().equals(request.getCode()) && 
                departmentRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException("Department with code " + request.getCode() + " already exists");
        }
        
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setCode(request.getCode());
        department.setSpecialtyArea(request.getSpecialtyArea());
        department.setHeadOfDepartment(request.getHeadOfDepartment());
        department.setContactInformation(request.getContactInformation());
        department.setActive(request.isActive());
        
        department = departmentRepository.save(department);
        return mapToResponseDTO(department);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Always perform soft delete by setting isActive to false
        // regardless of whether there are associated users/enrollments
        department.setActive(false);
        departmentRepository.save(department);
    }
    
    // Updated mapper method using builder pattern
    private DepartmentDTO.Response mapToResponseDTO(Department department) {
        return DepartmentDTO.Response.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .code(department.getCode())
                .specialtyArea(department.getSpecialtyArea())
                .headOfDepartment(department.getHeadOfDepartment())
                .contactInformation(department.getContactInformation())
                .isActive(department.isActive())
                .userCount(department.getUsers() != null ? department.getUsers().size() : 0)
                .courseCount(department.getCourses() != null ? department.getCourses().size() : 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentDTO.Response> searchDepartments(String keyword, boolean activeOnly, Pageable pageable) {
        Page<Department> departments;
        if (keyword == null || keyword.isEmpty()) {
            if (activeOnly) {
                departments = departmentRepository.findByIsActiveTrue(pageable);
            } else {
                departments = departmentRepository.findAll(pageable);
            }
        } else {
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            if (activeOnly) {
                departments = departmentRepository.searchActiveByKeyword(searchTerm, pageable);
            } else {
                departments = departmentRepository.searchByKeyword(searchTerm, pageable);
            }
        }
        return departments.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public DepartmentDTO.Response updateDepartmentStatus(Long id, boolean active) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        department.setActive(active);
        return mapToResponseDTO(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDepartmentCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", departmentRepository.count());
        counts.put("active", departmentRepository.countByIsActiveTrue());
        counts.put("inactive", departmentRepository.countByIsActiveFalse());
        return counts;
    }

    @Override
    @Transactional
    public List<DepartmentDTO.Response> batchCreateDepartments(List<DepartmentDTO.Request> requests) {
        List<Department> departments = new ArrayList<>();
        
        for (DepartmentDTO.Request request : requests) {
            // Validate unique name and code
            if (departmentRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Department with name " + request.getName() + " already exists");
            }
            
            if (departmentRepository.existsByCode(request.getCode())) {
                throw new ResourceAlreadyExistsException("Department with code " + request.getCode() + " already exists");
            }
            
            Department department = new Department();
            department.setName(request.getName());
            department.setDescription(request.getDescription());
            department.setCode(request.getCode());
            department.setSpecialtyArea(request.getSpecialtyArea());
            department.setHeadOfDepartment(request.getHeadOfDepartment());
            department.setContactInformation(request.getContactInformation());
            department.setActive(request.isActive());
            
            departments.add(department);
        }
        
        departments = departmentRepository.saveAll(departments);
        return departments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}