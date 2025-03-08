package com.example.lms.user.controller;

import com.example.lms.user.dto.BatchUserImportDTO;
import com.example.lms.user.service.BatchImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users/batch")
@RequiredArgsConstructor
public class BatchImportController {

    private final BatchImportService batchImportService;

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importUsers(
            @Valid @RequestBody List<BatchUserImportDTO> users) {
        return ResponseEntity.ok(batchImportService.importUsers(users));
    }
}