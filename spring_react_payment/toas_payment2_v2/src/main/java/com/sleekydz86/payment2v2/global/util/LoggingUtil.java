package com.sleekydz86.payment2v2.global.util;

import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;

public final class LoggingUtil {
    
    private LoggingUtil() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    public static void setContext(String key, String value) {
        MDC.put(key, value);
    }

    public static void setContext(Map<String, String> context) {
        context.forEach(MDC::put);
    }

    public static void clearContext() {
        MDC.clear();
    }

    public static <T> T executeWithContext(String key, String value, Supplier<T> supplier) {
        try {
            setContext(key, value);
            return supplier.get();
        } finally {
            MDC.remove(key);
        }
    }

    public static <T> T executeWithContext(Map<String, String> context, Supplier<T> supplier) {
        try {
            setContext(context);
            return supplier.get();
        } finally {
            context.keySet().forEach(MDC::remove);
        }
    }

    public static void executeWithContext(String key, String value, Runnable runnable) {
        try {
            setContext(key, value);
            runnable.run();
        } finally {
            MDC.remove(key);
        }
    }
}

