package com.sleekydz86.toaspayment.application.util;

import com.sleekydz86.toaspayment.domain.order.OrderStatus;
import com.sleekydz86.toaspayment.domain.order.PaymentMethod;

import java.util.regex.Pattern;

public class OrderDisplayUtil {
    
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-f]{7,8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        Pattern.CASE_INSENSITIVE
    );
    
    public static boolean isUuidFormat(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return false;
        }
        return UUID_PATTERN.matcher(orderId).matches();
    }
    
    public static boolean isOrdersFormat(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return false;
        }
        return orderId.startsWith("orders-");
    }
    
    public static String getDisplayOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return "-";
        }
        if (isOrdersFormat(orderId)) {
            return orderId;
        }
        if (isUuidFormat(orderId)) {
            return orderId;
        }
        return orderId;
    }
    
    public static String getOriginalOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        if (isUuidFormat(orderId)) {
            return orderId;
        }
        if (isOrdersFormat(orderId)) {
            return null;
        }
        return orderId;
    }
    
    public static String getStatusDisplayName(String status) {
        if (status == null) {
            return "-";
        }
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            return getStatusDisplayName(orderStatus);
        } catch (IllegalArgumentException e) {
            return status;
        }
    }
    
    public static String getStatusDisplayName(OrderStatus status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case PENDING -> "대기";
            case DONE -> "완료";
            case ABORTED -> "취소됨";
            case REFUND_REQUESTED -> "환불 요청";
            case REFUNDED -> "환불됨";
            case REFUND_FAILED -> "환불 실패";
        };
    }
    
    public static String getPaymentMethodDisplayName(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "-";
        }
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethod);
            return getPaymentMethodDisplayName(method);
        } catch (IllegalArgumentException e) {
            return paymentMethod;
        }
    }
    
    public static String getPaymentMethodDisplayName(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return "-";
        }
        return switch (paymentMethod) {
            case CARD -> "카드";
            case VIRTUAL_ACCOUNT -> "가상계좌";
            case MOBILE -> "휴대폰";
            case BANK_TRANSFER -> "계좌이체";
            case EASY_PAY -> "간편결제";
        };
    }
}

