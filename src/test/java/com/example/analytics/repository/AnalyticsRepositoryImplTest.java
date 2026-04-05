package com.example.analytics.repository;

import com.example.analytics.dto.AnalyticsResponseDto;
import com.example.analytics.dto.KpiResponseDto;
import com.example.analytics.exception.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyticsRepositoryImplTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private AnalyticsRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AnalyticsRepositoryImpl(jdbcTemplate);
    }

    @Test
    void shouldMapKpiSummaryResultForClientFilter() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(KpiResponseDto.builder()
                        .totalOrders(10L)
                        .totalRevenue(new BigDecimal("2500.75"))
                        .avgRevenuePerOrder(new BigDecimal("250.08"))
                        .build());

        KpiResponseDto response = repository.fetchKpiSummary("Acme");

        assertThat(response.getTotalOrders()).isEqualTo(10L);
        assertThat(response.getTotalRevenue()).isEqualByComparingTo("2500.75");
        assertThat(response.getAvgRevenuePerOrder()).isEqualByComparingTo("250.08");
    }

    @Test
    void shouldReturnEmptyKpiWhenNoRowsExist() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenThrow(new EmptyResultDataAccessException(1));

        KpiResponseDto response = repository.fetchKpiSummary(null);

        assertThat(response.getTotalOrders()).isZero();
        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAvgRevenuePerOrder()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnAggregatedAnalyticsLists() {
        when(jdbcTemplate.query(eq(QueryConstants.DAILY_ANALYTICS_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.DailyDto.builder().build()));
        when(jdbcTemplate.query(eq(QueryConstants.MONTHLY_ANALYTICS_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.MonthlyDto.builder().build()));
        when(jdbcTemplate.query(eq(QueryConstants.BY_CODE_ANALYTICS_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.CodeStatsDto.builder().code("ABC").build()));
        when(jdbcTemplate.query(eq(QueryConstants.DETAIL_ANALYTICS_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenReturn(List.of(com.example.analytics.dto.DetailDto.builder().code("ABC").build()));

        AnalyticsResponseDto response = repository.fetchAnalytics(null);

        assertThat(response.getDaily()).hasSize(1);
        assertThat(response.getMonthly()).hasSize(1);
        assertThat(response.getByCode()).hasSize(1);
        assertThat(response.getDetail()).hasSize(1);
        assertThat(response.getByCode().get(0).getCode()).isEqualTo("ABC");
    }

    @Test
    void shouldThrowDatabaseExceptionForInvalidAnalyticsQuery() {
        when(jdbcTemplate.query(eq(QueryConstants.DAILY_ANALYTICS_QUERY), any(org.springframework.jdbc.core.RowMapper.class), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> repository.fetchAnalytics("Acme"))
                .isInstanceOf(DatabaseException.class)
                .hasMessageContaining("Failed to execute analytics queries.");
    }
}
