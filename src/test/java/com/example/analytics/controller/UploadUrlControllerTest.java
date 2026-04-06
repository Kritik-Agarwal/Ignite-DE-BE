package com.example.analytics.controller;

import com.example.analytics.dto.UploadUrlResponseDto;
import com.example.analytics.exception.GlobalExceptionHandler;
import com.example.analytics.service.UploadUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UploadUrlController.class)
@Import(GlobalExceptionHandler.class)
class UploadUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadUrlService uploadUrlService;

    @Test
    void shouldReturnUploadUrl() throws Exception {
        when(uploadUrlService.generateUploadUrl(any())).thenReturn(UploadUrlResponseDto.builder()
                .uploadUrl("https://example.com/upload")
                .fileKey("raw/clients/alpha/orders.csv")
                .requiredHeaders(Map.of("x-amz-server-side-encryption", "AES256"))
                .build());

        mockMvc.perform(post("/api/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"fileName\": \"orders.csv\",
                                  \"fileType\": \"textcsv\",
                                  \"clientName\": \"alpha\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("https://example.com/upload"))
                .andExpect(jsonPath("$.fileKey").value("raw/clients/alpha/orders.csv"))
                .andExpect(jsonPath("$.requiredHeaders['x-amz-server-side-encryption']").value("AES256"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"fileName\": \"\",
                                  \"clientName\": \"alpha\"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("fileName is required."));
    }
}

