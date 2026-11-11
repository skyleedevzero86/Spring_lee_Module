package com.sleekydz86.storagemigration.global.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class FileNames {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private FileNames() {
    }

    public static String sanitize(String originalName) {
        String name = originalName == null ? "file" : originalName;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        String cleaned = name.replaceAll("[\\p{Cntrl}:*?\"<>|]", "_").trim();
        return cleaned.isEmpty() ? "file" : cleaned;
    }

    public static String objectKey(String id, String originalName, java.time.Instant at) {
        String datePath = LocalDate.ofInstant(at, ZoneId.systemDefault()).format(DATE_PATH);
        return "uploads/" + datePath + "/" + id + "-" + sanitize(originalName);
    }

    public static String nameOf(String objectKey) {
        int slash = objectKey.lastIndexOf('/');
        return slash >= 0 ? objectKey.substring(slash + 1) : objectKey;
    }
}
