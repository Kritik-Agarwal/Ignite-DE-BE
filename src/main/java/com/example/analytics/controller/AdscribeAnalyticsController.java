package com.example.analytics.controller;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;
import com.example.analytics.service.AdscribeAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/adscribe")
@Tag(name = "Adscribe Analytics", description = "Operations for Adscribe performance dashboard")
public class AdscribeAnalyticsController {

    private final AdscribeAnalyticsService adscribeAnalyticsService;

    public AdscribeAnalyticsController(AdscribeAnalyticsService adscribeAnalyticsService) {
        this.adscribeAnalyticsService = adscribeAnalyticsService;
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get Adscribe analytics", description = "Returns KPI, daily performance, revenue by client, top shows, and detailed Adscribe rows for a single date or date range.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adscribe analytics returned successfully",
                    content = @Content(schema = @Schema(implementation = AdscribeAnalyticsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AdscribeAnalyticsResponseDto> getAnalytics(
            @Parameter(description = "Start date in yyyy-MM-dd format", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Optional end date in yyyy-MM-dd format. If omitted, startDate is used.", example = "2025-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adscribeAnalyticsService.getAnalytics(startDate, endDate));
    }
}
