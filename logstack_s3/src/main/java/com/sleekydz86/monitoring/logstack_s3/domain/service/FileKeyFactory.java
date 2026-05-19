package com.sleekydz86.monitoring.logstack_s3.domain.service;

import java.util.UUID;

public final class FileKeyFactory {

    private FileKeyFactory() {
    }

    public static String uploadKey(String originalFilename) {
        return "uploads/" + UUID.randomUUID() + "_" + sanitize(originalFilename);
    }

    public static String thumbnailKey() {
        return "thumbnails/" + UUID.randomUUID() + ".jpg";
    }

    public static String sanitize(String filename) {
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static String defaultFilename(String original) {
        return original == null || original.isBlank() ? "unknown" : original;
    }

    public static String defaultContentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }
}
