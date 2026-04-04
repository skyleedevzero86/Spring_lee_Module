package com.sleekydz86.monitoring.backend.monitoring.application.support;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public enum DashboardWindow {
    REAL_TIME("15s", "최근 15초 ~ 5분", "15 seconds", Duration.ofMinutes(5), DateTimeFormatter.ofPattern("HH:mm:ss", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul"))),
    HOURLY("1h", "최근 1시간", "5 minutes", Duration.ofHours(1), DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul"))),
    DAILY("24h", "최근 24시간", "2 hours", Duration.ofHours(24), DateTimeFormatter.ofPattern("MM.dd HH:mm", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul"))),
    WEEKLY("7d", "최근 7일", "14 hours", Duration.ofDays(7), DateTimeFormatter.ofPattern("MM.dd", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul"))),
    MONTHLY("30d", "최근 30일", "5 days", Duration.ofDays(30), DateTimeFormatter.ofPattern("MM.dd", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul")));

    private final String key;
    private final String rangeLabel;
    private final String bucketInterval;
    private final Duration range;
    private final DateTimeFormatter formatter;

    DashboardWindow(String key, String rangeLabel, String bucketInterval, Duration range, DateTimeFormatter formatter) {
        this.key = key;
        this.rangeLabel = rangeLabel;
        this.bucketInterval = bucketInterval;
        this.range = range;
        this.formatter = formatter;
    }

    public static DashboardWindow fromKey(String key) {
        for (DashboardWindow value : values()) {
            if (value.key.equalsIgnoreCase(key)) {
                return value;
            }
        }
        return REAL_TIME;
    }

    public String key() {
        return this.key;
    }

    public String rangeLabel() {
        return this.rangeLabel;
    }

    public String bucketInterval() {
        return this.bucketInterval;
    }

    public Duration range() {
        return this.range;
    }

    public Instant since(Instant anchor) {
        return anchor.minus(this.range);
    }

    public String formatLabel(Instant instant) {
        return this.formatter.format(instant);
    }
}
