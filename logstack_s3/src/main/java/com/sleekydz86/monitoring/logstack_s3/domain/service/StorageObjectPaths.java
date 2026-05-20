package com.sleekydz86.monitoring.logstack_s3.domain.service;

public final class StorageObjectPaths {

    public static final String PREFIX_ALL = "all";
    public static final String PREFIX_UPLOADS = "uploads";
    public static final String PREFIX_THUMBNAILS = "thumbnails";

    private StorageObjectPaths() {
    }

    public static String keyPrefix(String filter) {
        if (PREFIX_UPLOADS.equals(filter)) {
            return "uploads/";
        }
        if (PREFIX_THUMBNAILS.equals(filter)) {
            return "thumbnails/";
        }
        return null;
    }

    public static String kindLabel(String key) {
        if (key.startsWith("uploads/")) {
            return "원본";
        }
        if (key.startsWith("thumbnails/")) {
            return "썸네일";
        }
        return "기타";
    }

    public static String displayName(String key) {
        int slash = key.lastIndexOf('/');
        String name = slash >= 0 ? key.substring(slash + 1) : key;
        if (key.startsWith("uploads/")) {
            int underscore = name.indexOf('_');
            if (underscore >= 0 && underscore < name.length() - 1) {
                return name.substring(underscore + 1);
            }
        }
        return name;
    }

    public static boolean isImageKey(String key) {
        String lower = key.toLowerCase();
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }

    public static boolean isThumbnailKey(String key) {
        return key != null && key.startsWith("thumbnails/");
    }

    public static java.util.Optional<String> thumbnailUuid(String thumbnailKey) {
        if (!isThumbnailKey(thumbnailKey)) {
            return java.util.Optional.empty();
        }
        String name = thumbnailKey.substring("thumbnails/".length());
        int dot = name.lastIndexOf('.');
        if (dot <= 0) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(name.substring(0, dot));
    }

    public static String uploadsPrefixForThumbnail(String thumbnailKey) {
        return thumbnailUuid(thumbnailKey)
                .map(uuid -> "uploads/" + uuid + "_")
                .orElse(null);
    }
}
