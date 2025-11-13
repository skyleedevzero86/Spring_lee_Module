package com.sleekydz86.payment2v2.global.constants;

public final class PaymentConstants {
    public static final String PAY_METHOD_CARD = "CARD";
    public static final String PAY_METHOD_TOSS_MONEY = "TOSS_MONEY";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String PAY_COMPLETE_STATUS = "PAY_COMPLETE";
    public static final int AMOUNT_PRECISION = 19;
    public static final int AMOUNT_SCALE = 2;

    private PaymentConstants() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
}
