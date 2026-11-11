package com.sleekydz86.storagemigration.global.util;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;

public final class Require {

    private Require() {
    }

    public static String notBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, field + " 값이 필요합니다.");
        }
        return value.trim();
    }

    public static <T> T notNull(T value, String field) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, field + " 값이 필요합니다.");
        }
        return value;
    }

    public static int port(Integer value, int defaultPort) {
        int port = value == null ? defaultPort : value;
        if (port < 1 || port > 65535) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "포트는 1~65535 사이여야 합니다.");
        }
        return port;
    }

    public static String maxLength(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, field + " 길이는 " + max + "자 이하여야 합니다.");
        }
        return value;
    }
}
