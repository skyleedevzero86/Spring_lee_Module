package com.sleekydz86.toaspayment.domain.paymentlog;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentLogType logType;

    @Column(nullable = false)
    private String message;

    @Column(length = 1000)
    private String details;

    private LocalDateTime createdAt;

    private PaymentLog(String orderId, Long memberId, PaymentLogType logType, String message, String details) {
        this.orderId = orderId;
        this.memberId = memberId;
        this.logType = logType;
        this.message = message;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    public static PaymentLog create(String orderId, Long memberId, PaymentLogType logType, String message) {
        return new PaymentLog(orderId, memberId, logType, message, null);
    }

    public static PaymentLog create(String orderId, Long memberId, PaymentLogType logType, String message, String details) {
        return new PaymentLog(orderId, memberId, logType, message, details);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

