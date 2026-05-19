package com.sleekydz86.monitoring.logstack_s3.application.view;

import java.time.LocalDateTime;

public record FileDetailView(
        String id,
        String originalFilename,
        String contentType,
        long size,
        LocalDateTime createdAt,
        String objectKey,
        String thumbnailUrl,
        String previewUrl,
        String downloadUrl,
        boolean image,
        boolean pdf
) {
}
