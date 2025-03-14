package com.example.lms.analytics.service;

import com.example.lms.analytics.model.SystemAnalytics;
import com.example.lms.analytics.repository.SystemAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SystemAnalyticsService {

    @Autowired
    private SystemAnalyticsRepository analyticsRepository;

    public List<SystemAnalytics> getAnalytics() {
        return analyticsRepository.findAll();
    }

    public void saveAnalytics(SystemAnalytics analytics) {
        analyticsRepository.save(analytics);
    }
}
