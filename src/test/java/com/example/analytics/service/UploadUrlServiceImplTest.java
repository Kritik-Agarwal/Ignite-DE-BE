package com.example.analytics.service;

import com.example.analytics.dto.UploadUrlRequestDto;
import com.example.analytics.dto.UploadUrlResponseDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadUrlServiceImplTest {

    private final S3Presigner s3Presigner = mock(S3Presigner.class);
    private final PresignedPutObjectRequest presignedPutObjectRequest = mock(PresignedPutObjectRequest.class);
    private final UploadUrlServiceImpl uploadUrlService = new UploadUrlServiceImpl(s3Presigner, "bucket-name", 15, "", "");

    @Test
    void shouldGeneratePresignedUrlUsingExtensionFromFileName() throws MalformedURLException {
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(new URL("https://example.com/upload"));
        when(presignedPutObjectRequest.httpRequest()).thenReturn(SdkHttpRequest.builder()
                .method(SdkHttpMethod.PUT)
                .uri(URI.create("https://example.com/upload"))
                .appendHeader("x-amz-server-side-encryption", "AES256")
                .appendHeader("host", "example.com")
                .build());

        UploadUrlResponseDto response = uploadUrlService.generateUploadUrl(UploadUrlRequestDto.builder()
                .fileName("orders.csv")
                .fileType("textcsv")
                .clientName("alpha")
                .build());

        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);

        assertThat(response.getUploadUrl()).isEqualTo("https://example.com/upload");
        assertThat(response.getFileKey()).isEqualTo("raw/clients/alpha/orders.csv");
        assertThat(response.getRequiredHeaders()).containsEntry("x-amz-server-side-encryption", "AES256");
        assertThat(response.getRequiredHeaders()).doesNotContainKey("host");
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentType()).isNull();
        assertThat(captor.getValue().putObjectRequest().key()).isEqualTo("raw/clients/alpha/orders.csv");
    }

    @Test
    void shouldDefaultExtensionToBinWhenFileNameHasNoExtension() throws MalformedURLException {
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(new URL("https://example.com/upload"));
        when(presignedPutObjectRequest.httpRequest()).thenReturn(SdkHttpRequest.builder()
                .method(SdkHttpMethod.PUT)
                .uri(URI.create("https://example.com/upload"))
                .build());

        UploadUrlResponseDto response = uploadUrlService.generateUploadUrl(UploadUrlRequestDto.builder()
                .fileName("orders")
                .clientName("beta")
                .build());

        assertThat(response.getFileKey()).isEqualTo("raw/clients/beta/orders.bin");
        assertThat(response.getRequiredHeaders()).isEmpty();
    }

    @Test
    void shouldRejectUnsupportedClient() {
        assertThatThrownBy(() -> uploadUrlService.generateUploadUrl(UploadUrlRequestDto.builder()
                .fileName("report.csv")
                .fileType("csv")
                .clientName("delta")
                .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("clientName must be one of: alpha, beta, gamma.");
    }
}

