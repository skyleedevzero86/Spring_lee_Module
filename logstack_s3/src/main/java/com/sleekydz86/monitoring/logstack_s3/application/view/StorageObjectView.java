package com.sleekydz86.monitoring.logstack_s3.application.view;

import java.time.Instant;

public record StorageObjectView(
        String key,
        String displayName,
        String kindLabel,
        String sizeLabel,
        Instant lastModified,
        String previewUrl,
        boolean image
) {
}
