package com.example.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlResponseDto {

    private String uploadUrl;
    private String fileKey;
    private Map<String, String> requiredHeaders;
}
