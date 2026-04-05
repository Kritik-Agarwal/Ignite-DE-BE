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
@Schema(description = "Revenue and order totals grouped by code")
public class CodeStatsDto {

    private String code;
    private BigDecimal revenue;
    private Long orders;
}
