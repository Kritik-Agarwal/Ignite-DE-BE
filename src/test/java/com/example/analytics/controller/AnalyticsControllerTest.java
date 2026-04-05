package com.example.analytics.controller;

import com.example.analytics.dto.AnalyticsResponseDto;
import com.example.analytics.dto.CodeStatsDto;
import com.example.analytics.dto.DailyDto;
import com.example.analytics.dto.DetailDto;
import com.example.analytics.dto.KpiResponseDto;
import com.example.analytics.dto.MonthlyDto;
import com.example.analytics.exception.GlobalExceptionHandler;
import com.example.analytics.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@Import(GlobalExceptionHandler.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    void shouldReturnAnalyticsWithoutClientFilter() throws Exception {
        AnalyticsResponseDto response = AnalyticsResponseDto.builder()
                .kpi(KpiResponseDto.builder().totalOrders(10L).totalRevenue(new BigDecimal("2500.50")).avgRevenuePerOrder(new BigDecimal("250.05")).build())
                .daily(List.of(DailyDto.builder().date(LocalDate.of(2025, 1, 1)).revenue(new BigDecimal("1000.00")).orders(5L).build()))
                .monthly(List.of(MonthlyDto.builder().month(LocalDateTime.of(2025, 1, 1, 0, 0)).revenue(new BigDecimal("5000.00")).build()))
                .byCode(List.of(CodeStatsDto.builder().code("ABC").revenue(new BigDecimal("5000.00")).orders(10L).build()))
                .detail(List.of(DetailDto.builder().orderDate(LocalDateTime.of(2025, 1, 1, 10, 15)).code("ABC").orders(2L).revenue(new BigDecimal("200.00")).revenuePerOrder(new BigDecimal("100.00")).build()))
                .build();

        when(analyticsService.getAnalytics(eq(null))).thenReturn(response);

        mockMvc.perform(get("/api/analytics").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpi.totalOrders").value(10))
                .andExpect(jsonPath("$.kpi.totalRevenue").value(2500.5))
                .andExpect(jsonPath("$.daily[0].date").value("2025-01-01"));
    }

    @Test
    void shouldReturnAnalyticsWithClientFilter() throws Exception {
        AnalyticsResponseDto response = AnalyticsResponseDto.builder()
                .kpi(KpiResponseDto.builder().totalOrders(5L).totalRevenue(new BigDecimal("1500.00")).avgRevenuePerOrder(new BigDecimal("300.00")).build())
                .build();

        when(analyticsService.getAnalytics(eq("Acme"))).thenReturn(response);

        mockMvc.perform(get("/api/analytics")
                        .param("client", "Acme")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpi.totalOrders").value(5))
                .andExpect(jsonPath("$.kpi.totalRevenue").value(1500.0));
    }
}
