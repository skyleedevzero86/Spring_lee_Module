package com.sleekydz86.monitoring.logstack_s3.domain.model;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, page, size, totalElements, totalPages);
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return totalPages == 0 || page >= totalPages - 1;
    }

    public <U> PageResult<U> map(Function<T, U> mapper) {
        return new PageResult<>(
                content.stream().map(mapper).toList(),
                page,
                size,
                totalElements,
                totalPages
        );
    }
}
