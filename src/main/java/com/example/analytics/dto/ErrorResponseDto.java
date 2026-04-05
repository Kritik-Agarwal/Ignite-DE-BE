package com.example.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response payload")
public class ErrorResponseDto {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
