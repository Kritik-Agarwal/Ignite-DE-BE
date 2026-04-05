package com.example.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Adscribe analytics response")
public class AdscribeAnalyticsResponseDto {

    private AdscribeKpiResponseDto kpi;
    private List<DailyPerformanceDto> daily;
    private List<ClientRevenueDto> byClient;
    private List<ShowRevenueDto> topShows;
    private List<AdscribeDetailDto> detail;
}
