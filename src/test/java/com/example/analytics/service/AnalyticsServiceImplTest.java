package com.example.analytics.service;

import com.example.analytics.dto.AnalyticsResponseDto;
import com.example.analytics.dto.KpiResponseDto;
import com.example.analytics.repository.AnalyticsRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsServiceImplTest {

    private final AnalyticsRepository analyticsRepository = mock(AnalyticsRepository.class);
    private final AnalyticsServiceImpl analyticsService = new AnalyticsServiceImpl(analyticsRepository);

    @Test
    void shouldUseUnfilteredQueryWhenClientIsNull() {
        KpiResponseDto kpi = KpiResponseDto.builder().totalOrders(1L).totalRevenue(BigDecimal.ONE).avgRevenuePerOrder(BigDecimal.ONE).build();
        AnalyticsResponseDto analytics = AnalyticsResponseDto.builder().build();

        when(analyticsRepository.fetchKpiSummary(null)).thenReturn(kpi);
        when(analyticsRepository.fetchAnalytics(null)).thenReturn(analytics);

        AnalyticsResponseDto response = analyticsService.getAnalytics(null);

        assertThat(response.getKpi()).isEqualTo(kpi);
        verify(analyticsRepository).fetchKpiSummary(null);
        verify(analyticsRepository).fetchAnalytics(null);
    }

    @Test
    void shouldNormalizeBlankClientToUnfiltered() {
        KpiResponseDto kpi = KpiResponseDto.builder().totalOrders(1L).totalRevenue(BigDecimal.ONE).avgRevenuePerOrder(BigDecimal.ONE).build();
        AnalyticsResponseDto analytics = AnalyticsResponseDto.builder().build();

        when(analyticsRepository.fetchKpiSummary(null)).thenReturn(kpi);
        when(analyticsRepository.fetchAnalytics(null)).thenReturn(analytics);

        AnalyticsResponseDto response = analyticsService.getAnalytics("   ");

        assertThat(response.getKpi()).isEqualTo(kpi);
        verify(analyticsRepository).fetchKpiSummary(null);
        verify(analyticsRepository).fetchAnalytics(null);
    }

    @Test
    void shouldUseFilteredQueryWhenClientIsProvided() {
        KpiResponseDto kpi = KpiResponseDto.builder().totalOrders(1L).totalRevenue(BigDecimal.ONE).avgRevenuePerOrder(BigDecimal.ONE).build();
        AnalyticsResponseDto analytics = AnalyticsResponseDto.builder().build();

        when(analyticsRepository.fetchKpiSummary("Acme")).thenReturn(kpi);
        when(analyticsRepository.fetchAnalytics("Acme")).thenReturn(analytics);

        AnalyticsResponseDto response = analyticsService.getAnalytics("  Acme  ");

        assertThat(response.getKpi()).isEqualTo(kpi);
        verify(analyticsRepository).fetchKpiSummary("Acme");
        verify(analyticsRepository).fetchAnalytics("Acme");
    }
}
