package com.example.analytics.controller;

import com.example.analytics.dto.UploadUrlRequestDto;
import com.example.analytics.dto.UploadUrlResponseDto;
import com.example.analytics.service.UploadUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Upload", description = "Operations for S3 upload URL generation")
public class UploadUrlController {

    private final UploadUrlService uploadUrlService;

    public UploadUrlController(UploadUrlService uploadUrlService) {
        this.uploadUrlService = uploadUrlService;
    }

    @PostMapping("/upload-url")
    @Operation(summary = "Generate presigned upload URL", description = "Generates an S3 presigned PUT URL and file key for a supported client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned URL generated successfully",
                    content = @Content(schema = @Schema(implementation = UploadUrlResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UploadUrlResponseDto> generateUploadUrl(@Valid @RequestBody UploadUrlRequestDto request) {
        return ResponseEntity.ok(uploadUrlService.generateUploadUrl(request));
    }
}
