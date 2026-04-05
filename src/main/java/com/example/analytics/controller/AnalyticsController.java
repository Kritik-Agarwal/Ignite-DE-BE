package com.example.analytics.controller;

import com.example.analytics.dto.AnalyticsResponseDto;
import com.example.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Analytics", description = "Operations for KPI and analytics reporting")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get analytics data", description = "Returns KPI, daily, monthly, by-code, and detail analytics for all data or for a specific client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics returned successfully",
                    content = @Content(schema = @Schema(implementation = AnalyticsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AnalyticsResponseDto> getAnalytics(
            @Parameter(description = "Optional client filter. If omitted, all client data is returned.", example = "Acme")
            @RequestParam(required = false) String client) {
        return ResponseEntity.ok(analyticsService.getAnalytics(client));
    }
}
