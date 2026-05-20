package com.sleekydz86.monitoring.logstack_s3.application.assembler;

import org.springframework.stereotype.Component;

import com.sleekydz86.monitoring.logstack_s3.application.view.StorageBucketView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StorageBucket;

@Component
public class StorageBucketViewAssembler {

    public StorageBucketView toView(StorageBucket bucket) {
        return new StorageBucketView(
                bucket.id(),
                bucket.bucketCode(),
                bucket.region(),
                bucket.displayName(),
                bucket.createdAt());
    }
}
