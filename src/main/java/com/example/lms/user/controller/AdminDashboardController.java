package com.example.lms.user.controller;

import com.example.lms.user.dto.AdminDashboardDTO;
import com.example.lms.user.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<AdminDashboardDTO> getDashboardData() {
        return ResponseEntity.ok(adminDashboardService.getDashboardData());
    }
}