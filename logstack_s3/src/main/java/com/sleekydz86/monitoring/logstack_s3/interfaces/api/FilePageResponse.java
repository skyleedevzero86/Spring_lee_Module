package com.sleekydz86.monitoring.logstack_s3.interfaces.api;

import java.util.List;

import com.sleekydz86.monitoring.logstack_s3.application.view.FileListItemView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;

public record FilePageResponse(
        List<FileListItemView> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static FilePageResponse from(PageResult<FileListItemView> page) {
        return new FilePageResponse(
                page.content(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
