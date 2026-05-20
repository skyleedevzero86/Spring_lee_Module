package com.sleekydz86.monitoring.logstack_s3.domain.model;

import java.time.LocalDateTime;

public record StorageBucket(
        long id,
        String bucketCode,
        String region,
        String displayName,
        LocalDateTime createdAt
) {
}
