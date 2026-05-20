package com.sleekydz86.monitoring.logstack_s3.domain.model;

import java.time.Instant;

public record ListedStorageObject(
        String key,
        long sizeBytes,
        Instant lastModified
) {
}
