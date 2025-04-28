package com.example.lms.content.service;

import com.example.lms.content.dto.ModuleDTO;
import com.example.lms.content.dto.ModuleOrderRequest;
import com.example.lms.content.mapper.ModuleMapper;
import com.example.lms.content.model.Module;
import com.example.lms.content.repository.ContentRepository;
import com.example.lms.content.repository.ModuleRepository;
import com.example.lms.course.model.Course;
import com.example.lms.course.repository.CourseRepository;
import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.content.model.Content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private ContentRepository contentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    /**
     * Get all modules
     * @return List of all modules
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> getAllModules() {
        List<Module> modules = moduleRepository.findAll();
        return ModuleMapper.toDTOList(modules);
    }
    
    /**
     * Get a specific module by ID
     * @param id Module ID
     * @return Module DTO
     * @throws ResourceNotFoundException if module not found
     */
    @Transactional(readOnly = true)
    public ModuleDTO getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
        return ModuleMapper.toDTO(module);
    }
    
    /**
     * Get all modules for a specific course
     * @param courseId Course ID
     * @return List of modules for the course
     * @throws ResourceNotFoundException if course not found
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> getModulesByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        List<Module> modules = moduleRepository.findByCourseOrderBySequenceAsc(course);
        return ModuleMapper.toDTOList(modules);
    }

    /**
     * Create a new module
     * @param moduleDTO Module data
     * @return Created module
     * @throws ResourceNotFoundException if course not found
     */
    @Transactional
    public ModuleDTO createModule(ModuleDTO moduleDTO) {
        Course course = courseRepository.findById(moduleDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + moduleDTO.getCourseId()));
        
        Module module = new Module();
        module.setTitle(moduleDTO.getTitle());
        module.setDescription(moduleDTO.getDescription());
        module.setCourse(course);
        
        // Set the sequence to be after the last module in the course
        Integer maxSequence = moduleRepository.findMaxSequenceByCourseId(moduleDTO.getCourseId());
        module.setSequence(maxSequence != null ? maxSequence + 1 : 1);
        
        Module savedModule = moduleRepository.save(module);
        return ModuleMapper.toDTO(savedModule);
    }

    /**
     * Update an existing module
     * @param id Module ID
     * @param moduleDTO Updated module data
     * @return Updated module
     * @throws ResourceNotFoundException if module not found
     */
    @Transactional
    public ModuleDTO updateModule(Long id, ModuleDTO moduleDTO) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
        
        module.setTitle(moduleDTO.getTitle());
        module.setDescription(moduleDTO.getDescription());
        
        if (moduleDTO.getSequence() != null) {
            module.setSequence(moduleDTO.getSequence());
        }
        
        if (moduleDTO.getStatus() != null) {
            module.setStatus(ModuleStatus.valueOf(moduleDTO.getStatus()));
        }
        
        if (moduleDTO.getReleaseDate() != null) {
            module.setReleaseDate(moduleDTO.getReleaseDate());
        }
        
        if (moduleDTO.getIsReleased() != null) {
            module.setIsReleased(moduleDTO.getIsReleased());
        }
        
        Module updatedModule = moduleRepository.save(module);
        return ModuleMapper.toDTO(updatedModule);
    }

    /**
     * Delete a module
     * @param id Module ID
     */
    @Transactional
    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }
    
    /**
     * Reorder modules
     * @param moduleOrderRequests List of modules with new sequence values
     * @return List of updated modules
     */
    @Transactional
    public List<ModuleDTO> reorderModules(List<ModuleOrderRequest> moduleOrderRequests) {
        List<Module> updatedModules = new ArrayList<>();
        
        for (ModuleOrderRequest request : moduleOrderRequests) {
            Module module = moduleRepository.findById(request.getModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + request.getModuleId()));
            
            module.setSequence(request.getNewSequence());
            updatedModules.add(module);
        }
        
        List<Module> savedModules = moduleRepository.saveAll(updatedModules);
        return ModuleMapper.toDTOList(savedModules);
    }
    
    /**
     * Get the order of contents within a module
     * @param moduleId Module ID
     * @return List of content order information
     * @throws ResourceNotFoundException if module not found
     */
    @Transactional(readOnly = true)
    public List<Object> getContentsOrder(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        List<Content> orderedContents = module.getContents().stream()
                .sorted(Comparator.comparing(Content::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        
        return orderedContents.stream()
                .map(content -> {
                    return new ContentOrderInfo(content.getId(), content.getOrder(), content.getTitle());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Private class to represent content order information
     */
    private static class ContentOrderInfo {
        private Long id;
        private Integer sequence;
        private String title;
        
        public ContentOrderInfo(Long id, Integer sequence, String title) {
            this.id = id;
            this.sequence = sequence;
            this.title = title;
        }
        
        public Long getId() { return id; }
        public Integer getSequence() { return sequence; }
        public String getTitle() { return title; }
    }
    
    /**
     * Create a new Module entity from a Module DTO
     * Helper method for CourseMapper
     * 
     * @param moduleDTO The module DTO
     * @param course The course entity
     * @return Module entity
     */
    public Module createFromDTO(ModuleDTO moduleDTO, Course course) {
        Module module = new Module();
        module.setTitle(moduleDTO.getTitle());
        module.setDescription(moduleDTO.getDescription());
        module.setSequence(moduleDTO.getSequence());
        module.setCourse(course);
        return module;
    }
}