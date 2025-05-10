package com.example.lms.Department.service;

import com.example.lms.Department.dto.DepartmentDTO;
import com.example.lms.Department.model.Department;
import com.example.lms.Department.repository.DepartmentRepository;
import com.example.lms.common.Exception.ResourceAlreadyExistsException;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.user.repository.UserRepository; // Changed from User to user
import com.example.lms.user.model.User;
import com.example.lms.Department.dto.DepartmentDTO.HeadDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    
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

         log.debug("Creating department with request: {}", request);
         log.debug("SpecialtyArea from request: {}", request.getSpecialtyArea());
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
        // REMOVE this line as it's using the old field
        // department.setHeadOfDepartment(request.getHeadOfDepartment());
        
        // Instead, handle the head relationship if headId is provided
        if (request.getHeadId() != null) {
            userRepository.findById(request.getHeadId()).ifPresent(department::setHead);
        }
        
        department.setContactInformation(request.getContactInformation());
        department.setActive(request.isActive());
        log.debug("Department before save: {}", department);
    department = departmentRepository.save(department);
    log.debug("Department after save: {}", department);
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
        
        // Only update head if headId is provided
        if (request.getHeadId() != null) {
            userRepository.findById(request.getHeadId()).ifPresent(department::setHead);
        } else {
            // If headId is not provided, ensure head is null (remove head)
            department.setHead(null);
        }
        
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
    DepartmentDTO.Response response = new DepartmentDTO.Response();
    response.setId(department.getId());
    response.setName(department.getName());
    response.setDescription(department.getDescription());
    response.setCode(department.getCode());
    response.setSpecialtyArea(department.getSpecialtyArea()); // Make sure this is included
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

    // Add this private method:
    private DepartmentDTO responseToDepartmentDTO(DepartmentDTO.Response response) {
        // Create and return a DepartmentDTO from the response
        DepartmentDTO dto = new DepartmentDTO();
        // Copy properties from response to dto
        return dto;
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
            
            // Only set head if headId is provided
            if (request.getHeadId() != null) {
                userRepository.findById(request.getHeadId()).ifPresent(department::setHead);
            }
            
            department.setContactInformation(request.getContactInformation());
            department.setActive(request.isActive());
            
            departments.add(department);
        }
        
        departments = departmentRepository.saveAll(departments);
        return departments.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUserCountForDepartment(Long departmentId) {
        return userRepository.countByDepartmentId(departmentId);
    }
    
    @Override
    public Map<Long, Long> getUserCountsForAllDepartments() {
        // Get counts for all departments in one efficient query
        List<Object[]> results = userRepository.countUsersByDepartment();
        
        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] result : results) {
            Long departmentId = (Long) result[0];
            Long count = (Long) result[1];
            countMap.put(departmentId, count);
        }
        
        return countMap;
    }
    
    @Override
    public List<DepartmentDTO.Response> getAllDepartmentsWithUserCounts() {
        List<Department> departments = departmentRepository.findAll();
        Map<Long, Long> userCounts = getUserCountsForAllDepartments();
        
        return departments.stream()
            .map(dept -> {
                DepartmentDTO.Response dto = mapToResponseDTO(dept);
                dto.setUserCount(userCounts.getOrDefault(dept.getId(), 0L));
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<DepartmentDTO.Response> getAllDepartmentsPageableWithUserCounts(Pageable pageable) {
        // 1. Get paginated departments
        Page<Department> departmentPage = departmentRepository.findAll(pageable);
        
        // 2. Get user counts for all departments in one efficient query
        Map<Long, Long> userCounts = getUserCountsForAllDepartments();
        
        // 3. Map departments to DTOs with user counts
        return departmentPage.map(dept -> {
            DepartmentDTO.Response dto = mapToResponseDTO(dept);
            // Add user count from our efficient query (overrides what mapToResponseDTO might have set)
            dto.setUserCount(userCounts.getOrDefault(dept.getId(), 0L));
            return dto;
        });
    }

    @Override
    @Transactional
    public DepartmentDTO.Response assignDepartmentHead(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        
        // Find the user to assign as head
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Set the head
        department.setHead(user);
        department = departmentRepository.save(department);
        
        // Return updated department - just return the response directly
        return mapToResponseDTO(department);
    }

    @Override
    @Transactional
    public DepartmentDTO.Response removeDepartmentHead(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        
        // Remove the head
        department.setHead(null);
        department = departmentRepository.save(department);
        
        // Return updated department - just return the response directly
        return mapToResponseDTO(department);
    }
}