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
@Schema(description = "KPI summary response")
public class KpiResponseDto {

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenuePerOrder;
}
