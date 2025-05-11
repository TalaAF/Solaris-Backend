package com.example.lms.content.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import com.example.lms.common.Exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ContentFileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    /**
     * Store a file and return the path
     * 
     * @param file MultipartFile from request
     * @return Path where the file was stored
     * @throws RuntimeException if file can't be stored
     */
    public String storeFile(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Clean up file name and add UUID to prevent duplicates
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            
            // Save the file
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Stored file {} to path {}", fileName, targetLocation);
            return uniqueFileName;
            
        } catch (IOException ex) {
            log.error("Could not store file", ex);
            throw new RuntimeException("Could not store file. Please try again.", ex);
        }
    }
    
    /**
     * Delete a file
     * 
     * @param filename Name of file to delete
     * @throws IOException if file can't be deleted
     */
    public void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        Files.deleteIfExists(filePath);
        log.info("Deleted file {}", filename);
    }
    
    /**
     * Load a file as a Resource
     * 
     * @param fileName Name of file to load
     * @return Resource containing the file
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            // Changed to use the available constructor
            log.error("Malformed URL for file: {}", fileName, ex);
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
    }
}