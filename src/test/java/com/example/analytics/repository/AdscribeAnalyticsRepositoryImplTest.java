package com.example.analytics.repository;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;
import com.example.analytics.dto.AdscribeKpiResponseDto;
import com.example.analytics.exception.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdscribeAnalyticsRepositoryImplTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private AdscribeAnalyticsRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AdscribeAnalyticsRepositoryImpl(jdbcTemplate);
    }

    @Test
    void shouldMapAdscribeKpiSummaryResult() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(AdscribeKpiResponseDto.builder()
                        .totalRevenue(new BigDecimal("2500.75"))
                        .totalOrders(10L)
                        .totalImpressions(500L)
                        .avgRevenuePerOrder(new BigDecimal("250.08"))
                        .avgRevenuePerImpression(new BigDecimal("5.00"))
                        .avgImpressionsPerOrder(new BigDecimal("50.00"))
                        .build());

        AdscribeKpiResponseDto response = repository.getKpiSummary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertThat(response.getTotalRevenue()).isEqualByComparingTo("2500.75");
        assertThat(response.getTotalOrders()).isEqualTo(10L);
        assertThat(response.getTotalImpressions()).isEqualTo(500L);
        assertThat(response.getAvgRevenuePerImpression()).isEqualByComparingTo("5.00");
    }

    @Test
    void shouldReturnEmptyKpiWhenNoRowsExist() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenThrow(new EmptyResultDataAccessException(1));

        AdscribeKpiResponseDto response = repository.getKpiSummary(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalOrders()).isZero();
        assertThat(response.getTotalImpressions()).isZero();
    }

    @Test
    void shouldReturnAnalyticsDatasets() {
        when(jdbcTemplate.query(eq(AdscribeQueryConstants.ADSCRIBE_DAILY_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.DailyPerformanceDto.builder().build()));
        when(jdbcTemplate.query(eq(AdscribeQueryConstants.ADSCRIBE_BY_CLIENT_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.ClientRevenueDto.builder().clientName("Client A").build()));
        when(jdbcTemplate.query(eq(AdscribeQueryConstants.ADSCRIBE_TOP_SHOWS_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.ShowRevenueDto.builder().showName("Show A").build()));
        when(jdbcTemplate.query(eq(AdscribeQueryConstants.ADSCRIBE_DETAIL_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.AdscribeDetailDto.builder().showName("Show A").build()));

        AdscribeAnalyticsResponseDto response = repository.getAnalytics(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertThat(response.getDaily()).hasSize(1);
        assertThat(response.getByClient()).hasSize(1);
        assertThat(response.getTopShows()).hasSize(1);
        assertThat(response.getDetail()).hasSize(1);
        assertThat(response.getTopShows().get(0).getShowName()).isEqualTo("Show A");
    }

    @Test
    void shouldThrowDatabaseExceptionForAnalyticsFailure() {
        when(jdbcTemplate.query(eq(AdscribeQueryConstants.ADSCRIBE_DAILY_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> repository.getAnalytics(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Failed to fetch Adscribe analytics data from Redshift.");
    }
}
