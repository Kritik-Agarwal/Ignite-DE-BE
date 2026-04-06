package com.example.analytics.service;

import com.example.analytics.dto.UploadUrlRequestDto;
import com.example.analytics.dto.UploadUrlResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class UploadUrlServiceImpl implements UploadUrlService {

    private static final Set<String> ALLOWED_CLIENTS = Set.of("alpha", "beta", "gamma");

    private final S3Presigner s3Presigner;
    private final String bucket;
    private final long presignDurationMinutes;
    private final String serverSideEncryption;
    private final String kmsKeyId;

    public UploadUrlServiceImpl(S3Presigner s3Presigner,
                                @Value("${aws.s3.bucket}") String bucket,
                                @Value("${aws.s3.presign-duration-minutes}") long presignDurationMinutes,
                                @Value("${aws.s3.server-side-encryption:}") String serverSideEncryption,
                                @Value("${aws.s3.kms-key-id:}") String kmsKeyId) {
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.presignDurationMinutes = presignDurationMinutes;
        this.serverSideEncryption = serverSideEncryption;
        this.kmsKeyId = kmsKeyId;
    }

    @Override
    public UploadUrlResponseDto generateUploadUrl(UploadUrlRequestDto request) {
        String normalizedClient = normalizeAndValidateClient(request.getClientName());
        String sanitizedBaseName = sanitizeBaseName(request.getFileName());
        String extension = getExtension(request.getFileName());
        String fileKey = "raw/clients/" + normalizedClient + "/" + sanitizedBaseName + "." + extension;

        try {
            PutObjectRequest putObjectRequest = buildPutObjectRequest(fileKey);
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            Map<String, String> requiredHeaders = extractRequiredHeaders(presignedRequest);

            log.info(
                    "Generated S3 presigned upload URL: bucket={}, clientName={}, fileKey={}, expiresInMinutes={}, signedHeaders={}",
                    bucket,
                    normalizedClient,
                    fileKey,
                    presignDurationMinutes,
                    requiredHeaders.keySet()
            );
            log.debug("Presigned upload URL details: url={}, requiredHeaders={}", presignedRequest.url(), requiredHeaders);

            return UploadUrlResponseDto.builder()
                    .uploadUrl(presignedRequest.url().toString())
                    .fileKey(fileKey)
                    .requiredHeaders(requiredHeaders)
                    .build();
        } catch (Exception ex) {
            log.error("Failed to generate presigned upload URL for bucket={}, clientName={}, fileName={}", bucket, normalizedClient, request.getFileName(), ex);
            throw ex;
        }
    }

    private PutObjectRequest buildPutObjectRequest(String fileKey) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey);

        if (StringUtils.hasText(serverSideEncryption)) {
            ServerSideEncryption encryption = ServerSideEncryption.fromValue(serverSideEncryption);
            builder.serverSideEncryption(encryption);

            if (encryption == ServerSideEncryption.AWS_KMS && StringUtils.hasText(kmsKeyId)) {
                builder.ssekmsKeyId(kmsKeyId);
            }
        }

        return builder.build();
    }

    private Map<String, String> extractRequiredHeaders(PresignedPutObjectRequest presignedRequest) {
        Map<String, String> requiredHeaders = new LinkedHashMap<>();

        presignedRequest.httpRequest().headers().forEach((headerName, values) -> {
            if (!"host".equalsIgnoreCase(headerName)) {
                requiredHeaders.put(headerName, firstValue(values));
            }
        });

        return requiredHeaders;
    }

    private String firstValue(List<String> values) {
        return values == null || values.isEmpty() ? "" : values.get(0);
    }

    private String normalizeAndValidateClient(String clientName) {
        String normalized = clientName == null ? null : clientName.trim().toLowerCase();
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("clientName is required.");
        }
        if (!ALLOWED_CLIENTS.contains(normalized)) {
            throw new IllegalArgumentException("clientName must be one of: alpha, beta, gamma.");
        }
        return normalized;
    }

    private String sanitizeBaseName(String fileName) {
        String trimmed = fileName == null ? "" : fileName.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("fileName is required.");
        }
        String withoutExtension = trimmed.replaceAll("\\.[^.]*$", "");
        String sanitized = withoutExtension.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("fileName must contain at least one valid character.");
        }
        return sanitized;
    }

    private String getExtension(String fileName) {
        String trimmed = fileName == null ? "" : fileName.trim();
        int lastDotIndex = trimmed.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == trimmed.length() - 1) {
            return "bin";
        }
        return trimmed.substring(lastDotIndex + 1).toLowerCase();
    }
}

