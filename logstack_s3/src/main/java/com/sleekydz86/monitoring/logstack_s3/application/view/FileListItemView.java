package com.sleekydz86.monitoring.logstack_s3.application.view;

import java.time.LocalDateTime;

public record FileListItemView(
        String id,
        String originalFilename,
        String contentType,
        long size,
        LocalDateTime createdAt,
        String thumbnailUrl,
        String bucketDisplayName,
        String region,
        String sizeLabel,
        String mediaType
) {
}
