package com.example.analytics.service;

import com.example.analytics.dto.UploadUrlRequestDto;
import com.example.analytics.dto.UploadUrlResponseDto;

public interface UploadUrlService {

    UploadUrlResponseDto generateUploadUrl(UploadUrlRequestDto request);
}
