package com.example.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed Adscribe performance row")
public class AdscribeDetailDto {

    private LocalDate reportDate;
    private String clientName;
    private String showName;
    private BigDecimal revenue;
    private Long orders;
    private Long impressions;
    private BigDecimal revenuePerOrder;
    private BigDecimal revenuePerImpression;
    private BigDecimal impressionsPerOrder;
}
