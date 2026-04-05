package com.example.analytics.service;

import com.example.analytics.dto.AnalyticsResponseDto;

public interface AnalyticsService {

    AnalyticsResponseDto getAnalytics(String client);
}
