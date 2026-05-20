package com.sleekydz86.monitoring.logstack_s3.application.query;

import java.util.Optional;

public record ListStorageBucketsQuery(String keyword, int page, int size) {

    public ListStorageBucketsQuery {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
    }

    public Optional<String> keywordOptional() {
        return Optional.ofNullable(keyword)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }
}
