package com.example.analytics.service;

import com.example.analytics.dto.AnalyticsResponseDto;
import com.example.analytics.dto.KpiResponseDto;
import com.example.analytics.repository.AnalyticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public AnalyticsResponseDto getAnalytics(String client) {
        String normalizedClient = normalizeClient(client);
        log.info("Fetching analytics with client filter={}", normalizedClient == null ? "ALL" : normalizedClient);

        KpiResponseDto kpi = analyticsRepository.fetchKpiSummary(normalizedClient);
        AnalyticsResponseDto analytics = analyticsRepository.fetchAnalytics(normalizedClient);
        analytics.setKpi(kpi);
        return analytics;
    }

    private String normalizeClient(String client) {
        if (client == null) {
            return null;
        }
        String trimmed = client.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
