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
@Schema(description = "Aggregated analytics response")
public class AnalyticsResponseDto {

    private KpiResponseDto kpi;
    private List<DailyDto> daily;
    private List<MonthlyDto> monthly;
    private List<CodeStatsDto> byCode;
    private List<DetailDto> detail;
}
