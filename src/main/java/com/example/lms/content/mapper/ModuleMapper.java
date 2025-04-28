package com.example.lms.content.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.lms.content.dto.ContentDTO;
import com.example.lms.content.dto.ModuleDTO;
import com.example.lms.content.model.Content;
import com.example.lms.content.model.Module;
import com.example.lms.content.model.ModuleStatus;
import com.example.lms.content.service.ContentService;

/**
 * Mapper for converting between Module entities and DTOs
 */
@Component
public class ModuleMapper {

    private static ContentService contentService;
    
    @Autowired
    public ModuleMapper(ContentService contentService) {
        ModuleMapper.contentService = contentService;
    }
    
    /**
     * Convert Module entity to ModuleDTO
     * 
     * @param module The module entity to convert
     * @return The ModuleDTO
     */
    public static ModuleDTO toDTO(Module module) {
        if (module == null) {
            return null;
        }
        
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setSequence(module.getSequence());
        dto.setStatus(module.getStatus() != null ? module.getStatus().name() : null);
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        dto.setReleaseDate(module.getReleaseDate());
        dto.setIsReleased(module.getIsReleased());
        
        if (module.getCourse() != null) {
            dto.setCourseId(module.getCourse().getId());
            dto.setCourseName(module.getCourse().getTitle());
        }
        
        // Convert contents if available - but without recursively including module details
        if (module.getContents() != null && contentService != null) {
            List<ContentDTO> contentDTOs = module.getContents().stream()
                .sorted((c1, c2) -> {
                    if (c1.getOrder() == null) return 1;
                    if (c2.getOrder() == null) return -1;
                    return c1.getOrder().compareTo(c2.getOrder());
                })
                .map(content -> contentService.convertToDTO(content))
                .collect(Collectors.toList());
            
            dto.setContents(contentDTOs);
        }
        
        return dto;
    }
    
    /**
     * Convert Module entity to ModuleDTO without contents
     * 
     * @param module The module entity to convert
     * @return The ModuleDTO without contents
     */
    public static ModuleDTO toSummaryDTO(Module module) {
        if (module == null) {
            return null;
        }
        
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setSequence(module.getSequence());
        dto.setStatus(module.getStatus() != null ? module.getStatus().name() : null);
        
        if (module.getCourse() != null) {
            dto.setCourseId(module.getCourse().getId());
            dto.setCourseName(module.getCourse().getTitle());
        }
        
        // Don't include contents to avoid recursion
        return dto;
    }
    
    /**
     * Convert a list of Module entities to ModuleDTO list
     * 
     * @param modules The list of module entities
     * @return List of ModuleDTOs
     */
    public static List<ModuleDTO> toDTOList(List<Module> modules) {
        return modules.stream()
            .map(ModuleMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert a list of Module entities to summary ModuleDTO list
     * 
     * @param modules The list of module entities
     * @return List of summary ModuleDTOs
     */
    public static List<ModuleDTO> toSummaryDTOList(List<Module> modules) {
        return modules.stream()
            .map(ModuleMapper::toSummaryDTO)
            .collect(Collectors.toList());
    }
}