package com.example.lms.Department.service;

import com.example.lms.Department.dto.DepartmentDTO;
import com.example.lms.Department.model.Department;
import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.common.Exception.ResourceAlreadyExistsException;
import com.example.lms.common.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        
        // Check if department has associated users before deletion
        if (!department.getUsers().isEmpty()) {
            // Soft delete by setting isActive to false
            department.setActive(false);
            departmentRepository.save(department);
        } else {
            departmentRepository.delete(department);
        }
    }
    
    // Utility method to map Department entity to ResponseDTO
    private DepartmentDTO.Response mapToResponseDTO(Department department) {
        return new DepartmentDTO.Response(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCode(),
                department.getSpecialtyArea(),
                department.getHeadOfDepartment(),
                department.getContactInformation(),
                department.isActive(),
                department.getUsers().size()
        );
    }
}