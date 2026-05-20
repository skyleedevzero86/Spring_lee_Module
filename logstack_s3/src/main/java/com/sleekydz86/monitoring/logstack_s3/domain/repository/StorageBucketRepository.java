package com.sleekydz86.monitoring.logstack_s3.domain.repository;

import java.util.Optional;

import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StorageBucket;

public interface StorageBucketRepository {

    PageResult<StorageBucket> search(Optional<String> keyword, int page, int size);

    Optional<StorageBucket> findByBucketCode(String bucketCode);
}
