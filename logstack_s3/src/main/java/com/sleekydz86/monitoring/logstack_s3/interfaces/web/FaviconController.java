package com.sleekydz86.monitoring.logstack_s3.interfaces.web;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.sleekydz86.monitoring.logstack_s3.infrastructure.web.SiteFaviconRenderer;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FaviconController {

    private final SiteFaviconRenderer faviconRenderer;

    @GetMapping(value = "favicon.ico", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> favicon() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(faviconRenderer.renderPng());
    }
}
