package com.example.lms.content.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lms.content.service.ModuleService;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderModules(@RequestBody List<Long> moduleIds) {
        moduleService.reorderModules(moduleIds);
        return ResponseEntity.ok().build();
    }
}