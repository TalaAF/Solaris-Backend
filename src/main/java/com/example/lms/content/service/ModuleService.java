package com.example.lms.content.service;

import com.example.lms.content.model.Module;
import com.example.lms.content.repository.ContentRepository;
import com.example.lms.content.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lms.common.Exception.ResourceNotFoundException;
import com.example.lms.content.model.Content;

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

    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    public Optional<Module> getModuleById(Long id) {
        return moduleRepository.findById(id);
    }

    public Module createModule(Module module) {
        return moduleRepository.save(module);
    }

    public Optional<Module> updateModule(Long id, Module updatedModule) {
        return moduleRepository.findById(id).map(module -> {
            module.setTitle(updatedModule.getTitle());
            module.setDescription(updatedModule.getDescription());
            return moduleRepository.save(module);
        });
    }

    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }
    
    public void reorderModules(List<Long> moduleIds) {
        List<Module> modules = moduleRepository.findAllById(moduleIds);
        for (int i = 0; i < modules.size(); i++) {
            modules.get(i).setSequence(i + 1);
        }
        moduleRepository.saveAll(modules); // Batch update
    }
    
    public boolean validateContentSequence(Long moduleId, List<Long> contentIds) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        List<Content> contents = module.getContents();
                if (contents.size() != contentIds.size()) {
            return false; }
    
        for (int i = 0; i < contents.size(); i++) {
            if (!contentIds.get(i).equals(contents.get(i).getId())) {
                return false; 
            }
        }
        
        return true; 
    }
    
    
    

    @Transactional
    public void reorderContents(Long moduleId, List<Long> contentIds) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        
        List<Content> contents = contentRepository.findAllById(contentIds);
        
        // Validate that all contents belong to the module
        if (contents.size() != contentIds.size()) {
            throw new IllegalArgumentException("One or more content IDs are invalid");
        }
        
        for (int i = 0; i < contentIds.size(); i++) {
            final int index = i;
            Long contentId = contentIds.get(index);
            contents.stream()
                    .filter(content -> content.getId().equals(contentId))
                    .findFirst()
                    .ifPresent(content -> {
                        content.setOrder(index + 1);
                        contentRepository.save(content);
                    });
        }
    }


    public List<Content> getContentsOrder(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id " + moduleId));

        return module.getContents().stream()
                .sorted(Comparator.comparingInt(content -> content.getModule().getSequence())) // ترتيب باستخدام sequence في Module
                .collect(Collectors.toList());
    }
    
}