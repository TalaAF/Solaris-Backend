package com.example.lms.content.service;

import com.example.lms.content.model.Module;
import com.example.lms.content.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.lms.content.model.Content;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

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
    
    
    

    public void reorderContents(Long moduleId, List<Long> contentIds) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
    
        List<Content> contents = module.getContents();
        for (int i = 0; i < contentIds.size(); i++) {
            Long contentId = contentIds.get(i);
            final int index = i;
            contents.stream()
                    .filter(content -> content.getId().equals(contentId))
                    .findFirst()
                    .ifPresent(content -> {
            // Update the order of the content within the module
          module.setSequence(index + 1); // Using sequence from Module

                    });
        }
        moduleRepository.save(module);
    }

    
}