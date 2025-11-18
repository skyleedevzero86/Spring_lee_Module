package com.sleekydz86.payment2v2.global.constants;

public final class ValidationConstants {
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_ORDER_NO_LENGTH = 50;
    public static final int MAX_PRODUCT_DESC_LENGTH = 255;
    public static final int MIN_AMOUNT = 1;
    public static final long MAX_AMOUNT = 1_000_000_000L;
    public static final int MIN_USER_ID = 1;

    private ValidationConstants() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
}
