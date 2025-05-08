package com.example.lms.Department.mapper;

import com.example.lms.Department.dto.DepartmentDTO;
import com.example.lms.Department.model.Department;
import com.example.lms.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {
    
    public DepartmentDTO.Response toResponseDto(Department department) {
        if (department == null) {
            return null;
        }
        
        DepartmentDTO.Response.ResponseBuilder builder = DepartmentDTO.Response.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .code(department.getCode())
                .contactInformation(department.getContactInformation())
                .active(department.isActive());
        
        // Add head information if present
        if (department.getHead() != null) {
            User head = department.getHead();
            // Create a HeadDTO instead of UserDTO
            DepartmentDTO.HeadDTO headDto = new DepartmentDTO.HeadDTO(
                head.getId(),
                head.getFullName(),
                head.getEmail()
            );
            builder.head(headDto);
        }
        
        return builder.build();
    }
}