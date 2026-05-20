package com.sleekydz86.monitoring.logstack_s3.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.sleekydz86.monitoring.logstack_s3.application.query.BrowseStorageQuery;
import com.sleekydz86.monitoring.logstack_s3.application.query.GetStoragePreviewQuery;
import com.sleekydz86.monitoring.logstack_s3.application.query.ListStorageBucketsQuery;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.BrowseStorageUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.GetStoragePreviewUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.ListStorageBucketsUseCase;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StorageViewController {

    private final ListStorageBucketsUseCase listStorageBucketsUseCase;
    private final BrowseStorageUseCase browseStorageUseCase;
    private final GetStoragePreviewUseCase getStoragePreviewUseCase;

    @GetMapping("/storage")
    public String buckets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        var result = listStorageBucketsUseCase.apply(new ListStorageBucketsQuery(keyword, page, size));
        model.addAttribute("page", result);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "storage-buckets";
    }

    @GetMapping("/storage/buckets/{bucketCode}")
    public String objects(
            @PathVariable String bucketCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = StorageObjectPaths.PREFIX_ALL) String prefix,
            Model model
    ) {
        var browse = browseStorageUseCase.apply(new BrowseStorageQuery(bucketCode, keyword, prefix, page, size));
        model.addAttribute("browse", browse);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("prefix", prefix);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "storage-objects";
    }

    @GetMapping("/storage/buckets/{bucketCode}/preview")
    public String preview(
            @PathVariable String bucketCode,
            @RequestParam("key") String key,
            Model model
    ) {
        var preview = getStoragePreviewUseCase.apply(new GetStoragePreviewQuery(bucketCode, key));
        model.addAttribute("preview", preview);
        return "storage-preview";
    }
}
