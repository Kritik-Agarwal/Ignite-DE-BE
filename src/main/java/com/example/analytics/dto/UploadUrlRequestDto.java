package com.example.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlRequestDto {

    @NotBlank(message = "fileName is required.")
    private String fileName;

    private String fileType;

    @NotBlank(message = "clientName is required.")
    private String clientName;
}
