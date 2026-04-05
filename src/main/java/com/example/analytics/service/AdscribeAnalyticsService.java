package com.example.analytics.service;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;

import java.time.LocalDate;

public interface AdscribeAnalyticsService {

    AdscribeAnalyticsResponseDto getAnalytics(LocalDate startDate, LocalDate endDate);
}
