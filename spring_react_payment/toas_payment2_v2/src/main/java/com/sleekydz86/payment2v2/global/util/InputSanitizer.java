package com.sleekydz86.payment2v2.global.util;

import java.util.regex.Pattern;

public final class InputSanitizer {
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|onerror|onload)"
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|</script>|javascript:|onerror=|onload=|onclick=|onmouseover=)"
    );
    private static final Pattern SQL_META_CHARS = Pattern.compile("[';\"\\\\]");

    private InputSanitizer() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input.trim();
        sanitized = removeSqlInjection(sanitized);
        sanitized = removeXss(sanitized);
        return sanitized;
    }

    public static String sanitizeForSql(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = sanitize(input);
        sanitized = SQL_META_CHARS.matcher(sanitized).replaceAll("");
        return sanitized;
    }

    public static boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public static boolean containsXss(String input) {
        if (input == null) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    private static String removeSqlInjection(String input) {
        return SQL_INJECTION_PATTERN.matcher(input).replaceAll("");
    }

    private static String removeXss(String input) {
        return XSS_PATTERN.matcher(input).replaceAll("");
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}



