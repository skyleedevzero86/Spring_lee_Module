package com.sleekydz86.monitoring.logstack_s3.application.query;

import java.util.Optional;

import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

public record BrowseStorageQuery(String keyword, String prefixFilter) {

    public BrowseStorageQuery {
        if (prefixFilter == null || prefixFilter.isBlank()) {
            prefixFilter = StorageObjectPaths.PREFIX_ALL;
        }
    }

    public Optional<String> keywordOptional() {
        return Optional.ofNullable(keyword)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }
}
