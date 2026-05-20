package com.sleekydz86.monitoring.logstack_s3.application.query;

import java.util.Optional;

import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

public record BrowseStorageQuery(
        String bucketCode,
        String keyword,
        String prefixFilter,
        int page,
        int size
) {

    public BrowseStorageQuery {
        if (prefixFilter == null || prefixFilter.isBlank()) {
            prefixFilter = StorageObjectPaths.PREFIX_ALL;
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 12;
        }
    }

    public Optional<String> keywordOptional() {
        return Optional.ofNullable(keyword)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }
}
