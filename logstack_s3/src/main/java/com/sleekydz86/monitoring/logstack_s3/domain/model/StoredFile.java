package com.sleekydz86.monitoring.logstack_s3.domain.model;

import java.time.LocalDateTime;
import java.util.Optional;

public record StoredFile(
        String id,
        String originalFilename,
        String objectKey,
        String thumbnailKey,
        String contentType,
        long size,
        LocalDateTime createdAt
) {

    public static StoredFile draft(
            String originalFilename,
            String objectKey,
            String thumbnailKey,
            String contentType,
            long size
    ) {
        return new StoredFile(
                null,
                originalFilename,
                objectKey,
                thumbnailKey,
                contentType,
                size,
                LocalDateTime.now()
        );
    }

    public StoredFile withId(String newId) {
        return new StoredFile(newId, originalFilename, objectKey, thumbnailKey, contentType, size, createdAt);
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isPdf() {
        return "application/pdf".equals(contentType);
    }

    public Optional<String> thumbnailKeyOptional() {
        return Optional.ofNullable(thumbnailKey).filter(key -> !key.isBlank());
    }
}
