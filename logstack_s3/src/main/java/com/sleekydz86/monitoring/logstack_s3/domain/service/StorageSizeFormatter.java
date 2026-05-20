package com.sleekydz86.monitoring.logstack_s3.domain.service;

public final class StorageSizeFormatter {

    private StorageSizeFormatter() {
    }

    public static String format(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " kB";
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
