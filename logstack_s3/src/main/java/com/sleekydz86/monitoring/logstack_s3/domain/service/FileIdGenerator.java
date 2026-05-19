package com.sleekydz86.monitoring.logstack_s3.domain.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FileIdGenerator {

    private static final DateTimeFormatter DATE_TIME_KEY = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private FileIdGenerator() {
    }

    public static String dateTimePrefix(String ownerPrefix, LocalDateTime uploadedAt) {
        return ownerPrefix + "_" + uploadedAt.format(DATE_TIME_KEY);
    }

    public static String formatId(String dateTimePrefix, long sequence) {
        String number = sequence < 10_000L
                ? String.format("%04d", sequence)
                : String.valueOf(sequence);
        return dateTimePrefix + "_" + number;
    }

    public static String nextId(String dateTimePrefix, long currentMaxSequence) {
        return formatId(dateTimePrefix, currentMaxSequence + 1);
    }
}
