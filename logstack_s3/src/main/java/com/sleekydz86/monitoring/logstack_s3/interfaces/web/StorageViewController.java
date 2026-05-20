package com.sleekydz86.monitoring.logstack_s3.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sleekydz86.monitoring.logstack_s3.application.query.BrowseStorageQuery;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.BrowseStorageUseCase;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StorageViewController {

    private final BrowseStorageUseCase browseStorageUseCase;

    @GetMapping("/storage")
    public String storage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = StorageObjectPaths.PREFIX_ALL) String prefix,
            Model model
    ) {
        var browse = browseStorageUseCase.apply(new BrowseStorageQuery(keyword, prefix));
        model.addAttribute("browse", browse);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("prefix", prefix);
        return "storage";
    }
}
