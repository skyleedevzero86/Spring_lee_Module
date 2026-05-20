package com.sleekydz86.monitoring.logstack_s3.application.view;

import java.util.List;

public record StorageBrowseView(
        String bucketName,
        int objectCount,
        List<StorageObjectView> objects
) {
}
