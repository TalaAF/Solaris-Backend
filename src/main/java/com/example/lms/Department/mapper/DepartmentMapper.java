package com.example.lms.Department.mapper;

import com.example.lms.Department.dto.DepartmentDTO;
import com.example.lms.Department.model.Department;
import com.example.lms.user.model.User;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DepartmentMapper {
    
    public DepartmentDTO.Response toResponseDto(Department department) {
        if (department == null) {
            return null;
        }
        log.debug("Mapping department to DTO with ID: {}", department.getId());
        log.debug("Department specialtyArea value: '{}'", department.getSpecialtyArea());

        // Use the direct object initialization approach instead of builder pattern
        DepartmentDTO.Response response = new DepartmentDTO.Response();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setDescription(department.getDescription());
        response.setCode(department.getCode());
        response.setSpecialtyArea(department.getSpecialtyArea());
        response.setContactInformation(department.getContactInformation());
        response.setActive(department.isActive());
        
        // Add user and course counts
        response.setUserCount(department.getUsers() != null ? 
            Long.valueOf(department.getUsers().size()) : 0L);
        response.setCourseCount(department.getCourses() != null ? 
            Long.valueOf(department.getCourses().size()) : 0L);
        
        // Add head information if present
        if (department.getHead() != null) {
            User head = department.getHead();
            response.setHead(new DepartmentDTO.HeadDTO(
                head.getId(),
                head.getFullName(),
                head.getEmail()
            ));
        }
        
        return response;
    }

    public Department toEntity(DepartmentDTO.Request request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setCode(request.getCode());
        department.setSpecialtyArea(request.getSpecialtyArea());
        department.setContactInformation(request.getContactInformation());
        department.setActive(request.isActive());
        // Head will be set separately
        return department;
    }
}