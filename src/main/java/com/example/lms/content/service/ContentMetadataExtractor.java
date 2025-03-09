package com.example.lms.content.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContentMetadataExtractor {

    public String extractFileType(MultipartFile file) {
        return file.getContentType();  // File type (eg application/pdf)

    }

    public long extractFileSize(MultipartFile file) {
        return file.getSize();  //File size in bytes

    }
}