package com.example.lms.content.service;

import com.example.lms.content.model.Module;
import com.example.lms.content.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}