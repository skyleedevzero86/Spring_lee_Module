package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StorageBucket;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.StorageBucketRepository;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.mapper.StorageBucketMapper;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StorageBucketRow;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisStorageBucketRepository implements StorageBucketRepository {

    private final StorageBucketMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<StorageBucket> search(Optional<String> keyword, int page, int size) {
        String kw = keyword.orElse(null);
        long total = mapper.count(kw);
        var rows = mapper.selectPage(kw, page * size, size).stream()
                .map(this::toDomain)
                .toList();
        return PageResult.of(rows, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StorageBucket> findByBucketCode(String bucketCode) {
        StorageBucketRow row = mapper.selectByBucketCode(bucketCode);
        return Optional.ofNullable(row).map(this::toDomain);
    }

    private StorageBucket toDomain(StorageBucketRow row) {
        return new StorageBucket(
                row.getId(),
                row.getBucketCode(),
                row.getRegion(),
                row.getDisplayName(),
                row.getCreatedAt());
    }
}
