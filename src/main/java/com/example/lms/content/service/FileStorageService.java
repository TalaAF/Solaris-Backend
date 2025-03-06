package com.example.lms.content.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class FileStorageService {

    @Value("${file.storage.location}")
    private String storageLocation;

    @Value("${file.storage.max-file-size}")
    private long maxFileSize;

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
        validateFileSize(file);
        validateFileType(file);
        scanForViruses(file);

        try {
            Path targetLocation = this.fileStorageLocation.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), targetLocation);
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new RuntimeException("File not found: " + fileName);
            }
            return resource;
        } catch (Exception ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds the allowed limit of " + maxFileSize + " bytes");
        }
    }

    private void validateFileType(MultipartFile file) {
        if (!allowedFileTypes.contains(file.getContentType())) {
            throw new RuntimeException("Invalid file type: " + file.getContentType());
        }
    }

    private void scanForViruses(MultipartFile file) {
        try {
            // تنفيذ فحص الفيروسات باستخدام ClamAV
            ClamAVClient clamAVClient = new ClamAVClient("localhost", 3310);
            byte[] reply = clamAVClient.scan(file.getBytes());
            if (!ClamAVClient.isCleanReply(reply)) {
                throw new RuntimeException("File is infected with a virus: " + file.getOriginalFilename());
            }
        } catch (Exception e) {
            throw new RuntimeException("Virus scan failed", e);
        }
    }
}