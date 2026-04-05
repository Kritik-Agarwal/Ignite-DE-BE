package com.example.analytics.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Raw detail row from staged_processed_orders")
public class DetailDto {

    @JsonAlias("order_date")
    private LocalDateTime orderDate;
    private String code;
    private Long orders;
    private BigDecimal revenue;

    @JsonAlias("revenue_per_order")
    private BigDecimal revenuePerOrder;
}
