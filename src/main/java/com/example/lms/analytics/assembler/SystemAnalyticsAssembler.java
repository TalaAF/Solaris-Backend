package com.example.lms.analytics.assembler;

import com.example.lms.analytics.dto.SystemAnalyticsDTO;
import com.example.lms.analytics.mapper.SystemAnalyticsMapper;
import com.example.lms.analytics.model.SystemAnalytics;

import java.util.List;
import java.util.stream.Collectors;

public class SystemAnalyticsAssembler {

    public static List<SystemAnalyticsDTO> toDTOList(List<SystemAnalytics> analyticsList) {
        return analyticsList.stream()
                   .map(SystemAnalyticsMapper::toDTO)
                   .collect(Collectors.toList());
    }
}
