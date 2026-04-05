package com.example.analytics.repository;

import com.example.analytics.dto.AnalyticsResponseDto;
import com.example.analytics.dto.KpiResponseDto;

public interface AnalyticsRepository {

    KpiResponseDto fetchKpiSummary(String client);

    AnalyticsResponseDto fetchAnalytics(String client);
}
