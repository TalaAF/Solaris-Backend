package com.example.lms.analytics.controller;

import com.example.lms.analytics.dto.SystemAnalyticsDTO;
import com.example.lms.analytics.service.SystemAnalyticsService;
import com.example.lms.analytics.assembler.SystemAnalyticsAssembler;
import com.example.lms.analytics.model.SystemAnalytics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class SystemAnalyticsController {

    @Autowired
    private SystemAnalyticsService analyticsService;

    @GetMapping
    public List<SystemAnalyticsDTO> getSystemAnalytics() {
        List<SystemAnalytics> analyticsData = analyticsService.getAnalytics();
        return SystemAnalyticsAssembler.toDTOList(analyticsData);
    }
}
