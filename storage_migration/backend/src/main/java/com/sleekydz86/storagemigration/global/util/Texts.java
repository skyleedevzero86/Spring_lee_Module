package com.sleekydz86.storagemigration.global.util;

public final class Texts {

    private Texts() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    public static String mask(String value) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return trimmed.substring(0, 2) + "****" + trimmed.substring(trimmed.length() - 2);
    }
}
