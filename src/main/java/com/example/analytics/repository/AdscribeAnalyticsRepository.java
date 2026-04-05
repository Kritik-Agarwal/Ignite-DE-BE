package com.example.analytics.repository;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;
import com.example.analytics.dto.AdscribeKpiResponseDto;

import java.time.LocalDate;

public interface AdscribeAnalyticsRepository {

    AdscribeKpiResponseDto getKpiSummary(LocalDate startDate, LocalDate endDate);

    AdscribeAnalyticsResponseDto getAnalytics(LocalDate startDate, LocalDate endDate);
}
