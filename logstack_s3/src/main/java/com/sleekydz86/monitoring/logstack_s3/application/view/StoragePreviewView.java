package com.sleekydz86.monitoring.logstack_s3.application.view;

public record StoragePreviewView(
        String bucketCode,
        String bucketDisplayName,
        String objectKey,
        String originalKey,
        String displayName,
        String previewUrl,
        boolean image
) {
}
