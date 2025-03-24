package com.example.lms.content.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.lms.common.Exception.ResourceNotFoundException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ContentFileStorageService {

    @Value("${file.storage.location}")
    private String storageLocation;

    private Path fileStorageLocation;
    private final List<String> allowedFileTypes = Arrays.asList(
        "image/jpeg", 
        "image/png", 
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create storage directory", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        validateFileType(file);
    
        String originalFilename = file.getOriginalFilename();
    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    String secureFilename = UUID.randomUUID().toString() + fileExtension;
       try {
        Path targetLocation = this.fileStorageLocation.resolve(secureFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return secureFilename; // Return only filename, not the entire path for security
    } catch (IOException ex) {
        throw new RuntimeException("Could not store file " + file.getOriginalFilename(), ex);
    }
    }

    public Resource loadFileAsResource(String fileName) {
       try {
        // Validate filename to prevent path traversal
        if (fileName.contains("..")) {
            throw new RuntimeException("Filename contains invalid path sequence: " + fileName);
        }
        
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
        return resource;
    } catch (Exception ex) {
        throw new RuntimeException("File not found: " + fileName, ex);
    }
    }

    private void validateFileType(MultipartFile file) {
        if (!allowedFileTypes.contains(file.getContentType())) {
            throw new RuntimeException("Invalid file type: " + file.getContentType());
        }
    }


    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Files.deleteIfExists(path); // Delete the file if it exists

        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + filePath, ex);
        }
    }

}