package com.example.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Adscribe KPI summary response")
public class AdscribeKpiResponseDto {

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalImpressions;
    private BigDecimal avgRevenuePerOrder;
    private BigDecimal avgRevenuePerImpression;
    private BigDecimal avgImpressionsPerOrder;
}
