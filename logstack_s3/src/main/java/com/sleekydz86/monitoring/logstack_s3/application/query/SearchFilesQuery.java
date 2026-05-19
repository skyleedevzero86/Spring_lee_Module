package com.sleekydz86.monitoring.logstack_s3.application.query;

public record SearchFilesQuery(String keyword, int page, int size) {

    public java.util.Optional<String> keywordOptional() {
        return java.util.Optional.ofNullable(keyword)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }
}
