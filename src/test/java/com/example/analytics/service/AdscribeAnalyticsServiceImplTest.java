package com.example.analytics.service;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;
import com.example.analytics.dto.AdscribeKpiResponseDto;
import com.example.analytics.repository.AdscribeAnalyticsRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdscribeAnalyticsServiceImplTest {

    private final AdscribeAnalyticsRepository adscribeAnalyticsRepository = mock(AdscribeAnalyticsRepository.class);
    private final AdscribeAnalyticsServiceImpl adscribeAnalyticsService = new AdscribeAnalyticsServiceImpl(adscribeAnalyticsRepository);

    @Test
    void shouldUseSingleDayFallbackWhenEndDateIsMissing() {
        AdscribeKpiResponseDto kpi = AdscribeKpiResponseDto.builder().totalRevenue(BigDecimal.ONE).build();
        AdscribeAnalyticsResponseDto analytics = AdscribeAnalyticsResponseDto.builder().build();

        when(adscribeAnalyticsRepository.getKpiSummary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1))).thenReturn(kpi);
        when(adscribeAnalyticsRepository.getAnalytics(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1))).thenReturn(analytics);

        AdscribeAnalyticsResponseDto response = adscribeAnalyticsService.getAnalytics(LocalDate.of(2025, 1, 1), null);

        assertThat(response.getKpi()).isEqualTo(kpi);
        verify(adscribeAnalyticsRepository).getKpiSummary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1));
        verify(adscribeAnalyticsRepository).getAnalytics(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1));
    }

    @Test
    void shouldRejectInvalidDateRange() {
        assertThatThrownBy(() -> adscribeAnalyticsService.getAnalytics(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDate must be before or equal to endDate.");
    }

    @Test
    void shouldAttachKpiToAnalyticsResponse() {
        AdscribeKpiResponseDto kpi = AdscribeKpiResponseDto.builder().totalRevenue(new BigDecimal("100.00")).build();
        AdscribeAnalyticsResponseDto analytics = AdscribeAnalyticsResponseDto.builder().build();

        when(adscribeAnalyticsRepository.getKpiSummary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2))).thenReturn(kpi);
        when(adscribeAnalyticsRepository.getAnalytics(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2))).thenReturn(analytics);

        AdscribeAnalyticsResponseDto response = adscribeAnalyticsService.getAnalytics(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2));

        assertThat(response.getKpi()).isEqualTo(kpi);
        verify(adscribeAnalyticsRepository).getKpiSummary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2));
        verify(adscribeAnalyticsRepository).getAnalytics(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2));
    }
}
