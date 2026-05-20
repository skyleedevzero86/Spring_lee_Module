package com.sleekydz86.monitoring.logstack_s3.application.view;

import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;

public record StorageBrowseView(
        String bucketCode,
        String bucketDisplayName,
        String region,
        PageResult<StorageObjectView> page
) {
}
