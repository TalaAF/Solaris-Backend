package com.example.lms.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFileStorageService {
    
    @Value("${app.file-storage.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${app.file-storage.allowed-types:image/jpeg,image/png,image/gif}")
    private String[] allowedContentTypes;
    
    @Value("${app.file-storage.max-file-size:2097152}") // Default 2MB
    private long maxFileSize;
    
    public String storeProfilePicture(MultipartFile file, Long userId) {
        validateFile(file);
        
        try {
            Path uploadPath = Paths.get(uploadDir, "profiles").toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            
            // Generate a unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String filename = userId + "_" + UUID.randomUUID() + fileExtension;
            
            // Save the file
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the relative path to the file
            return "/profiles/" + filename;
            
        } catch (IOException ex) {
            log.error("Could not store file", ex);
            throw new RuntimeException("Could not store file. Please try again.", ex);
        }
    }
    
    public void deleteProfilePicture(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Could not delete file", ex);
        }
    }
    
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        // Check content type
        String contentType = file.getContentType();
        boolean isAllowedType = false;
        
        if (contentType != null) {
            for (String allowedType : allowedContentTypes) {
                if (contentType.equals(allowedType)) {
                    isAllowedType = true;
                    break;
                }
            }
        }
        
        if (!isAllowedType) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + 
                                              String.join(", ", allowedContentTypes));
        }
    }
}