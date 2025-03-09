package com.example.lms.content.controller;

import com.example.lms.content.service.ContentService;
import com.example.lms.content.service.ContentFileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final ContentFileStorageService fileStorageService;
    
    
    @Autowired
    private ContentService contentService;
    
    public FileController(ContentFileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileStorageService.storeFile(file);
            return ResponseEntity.ok("File uploaded successfully: " + filePath);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}