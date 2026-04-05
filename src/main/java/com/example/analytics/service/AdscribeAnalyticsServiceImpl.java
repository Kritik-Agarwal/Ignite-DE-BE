package com.example.analytics.service;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;
import com.example.analytics.dto.AdscribeKpiResponseDto;
import com.example.analytics.repository.AdscribeAnalyticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class AdscribeAnalyticsServiceImpl implements AdscribeAnalyticsService {

    private final AdscribeAnalyticsRepository adscribeAnalyticsRepository;

    public AdscribeAnalyticsServiceImpl(AdscribeAnalyticsRepository adscribeAnalyticsRepository) {
        this.adscribeAnalyticsRepository = adscribeAnalyticsRepository;
    }

    @Override
    public AdscribeAnalyticsResponseDto getAnalytics(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = validateAndBuildRange(startDate, endDate);
        log.info("Fetching Adscribe analytics with startDate={}, endDate={}, effectiveEndDate={}",
                startDate, endDate, dateRange.endDate());

        AdscribeKpiResponseDto kpi = adscribeAnalyticsRepository.getKpiSummary(dateRange.startDate(), dateRange.endDate());
        AdscribeAnalyticsResponseDto analytics = adscribeAnalyticsRepository.getAnalytics(dateRange.startDate(), dateRange.endDate());
        analytics.setKpi(kpi);
        return analytics;
    }

    private DateRange validateAndBuildRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required.");
        }

        LocalDate effectiveEndDate = endDate == null ? startDate : endDate;
        if (startDate.isAfter(effectiveEndDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate.");
        }

        return new DateRange(startDate, effectiveEndDate);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
