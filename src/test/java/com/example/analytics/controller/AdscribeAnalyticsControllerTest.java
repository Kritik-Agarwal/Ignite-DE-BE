package com.example.analytics.controller;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;
import com.example.analytics.dto.AdscribeDetailDto;
import com.example.analytics.dto.AdscribeKpiResponseDto;
import com.example.analytics.dto.ClientRevenueDto;
import com.example.analytics.dto.DailyPerformanceDto;
import com.example.analytics.dto.ShowRevenueDto;
import com.example.analytics.exception.GlobalExceptionHandler;
import com.example.analytics.service.AdscribeAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdscribeAnalyticsController.class)
@Import(GlobalExceptionHandler.class)
class AdscribeAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdscribeAnalyticsService adscribeAnalyticsService;

    @Test
    void shouldReturnAdscribeAnalyticsForSingleDay() throws Exception {
        AdscribeAnalyticsResponseDto response = AdscribeAnalyticsResponseDto.builder()
                .kpi(AdscribeKpiResponseDto.builder().totalRevenue(new BigDecimal("100.00")).totalOrders(2L).totalImpressions(20L).build())
                .daily(List.of(DailyPerformanceDto.builder().date(LocalDate.of(2025, 1, 1)).revenue(new BigDecimal("100.00")).orders(2L).impressions(20L).build()))
                .byClient(List.of(ClientRevenueDto.builder().clientName("Client A").revenue(new BigDecimal("100.00")).build()))
                .topShows(List.of(ShowRevenueDto.builder().showName("Show A").revenue(new BigDecimal("100.00")).build()))
                .detail(List.of(AdscribeDetailDto.builder().reportDate(LocalDate.of(2025, 1, 1)).clientName("Client A").showName("Show A").revenue(new BigDecimal("100.00")).orders(2L).impressions(20L).revenuePerOrder(new BigDecimal("50.00")).revenuePerImpression(new BigDecimal("5.00")).impressionsPerOrder(new BigDecimal("10.00")).build()))
                .build();

        when(adscribeAnalyticsService.getAnalytics(eq(LocalDate.of(2025, 1, 1)), eq(null)))
                .thenReturn(response);

        mockMvc.perform(get("/api/adscribe/analytics")
                        .param("startDate", "2025-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kpi.totalRevenue").value(100.0))
                .andExpect(jsonPath("$.daily[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$.byClient[0].clientName").value("Client A"))
                .andExpect(jsonPath("$.topShows[0].showName").value("Show A"));
    }

    @Test
    void shouldReturnBadRequestForInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/adscribe/analytics")
                        .param("startDate", "2025/01/01"))
                .andExpect(status().isBadRequest());
    }
}
