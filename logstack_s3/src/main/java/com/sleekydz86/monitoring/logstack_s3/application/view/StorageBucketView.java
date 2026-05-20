package com.sleekydz86.monitoring.logstack_s3.application.view;

import java.time.LocalDateTime;

public record StorageBucketView(
        long id,
        String bucketCode,
        String region,
        String displayName,
        LocalDateTime createdAt
) {
}
