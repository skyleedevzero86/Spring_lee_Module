package com.sleekydz86.monitoring.logstack_s3.domain.model;

import java.time.LocalDateTime;

public record StoredFileSummary(
        String id,
        String originalFilename,
        String objectKey,
        String thumbnailKey,
        String contentType,
        long size,
        LocalDateTime createdAt,
        String bucketCode,
        String region,
        String bucketDisplayName,
        String sizeLabel,
        String mediaType
) {

    public StoredFile toStoredFile() {
        return new StoredFile(
                id,
                originalFilename,
                objectKey,
                thumbnailKey,
                contentType,
                size,
                createdAt
        );
    }
}
