package com.sleekydz86.passykey.global.util;

import com.webauthn4j.util.Base64UrlUtil;

public final class Base64UrlConverter {
    private Base64UrlConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static byte[] decode(String base64Url) {
        return Base64UrlUtil.decode(base64Url);
    }

    public static String encode(byte[] bytes) {
        return Base64UrlUtil.encodeToString(bytes);
    }
}
