package com.sleekydz86.toaspayment.domain.order;

import com.sleekydz86.toaspayment.domain.order.valueobject.Money;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private OrderId orderId;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private Long memberId;

    @Embedded
    private Money finalAmount;

    private String paymentKey;

    @Column(name = "original_order_id")
    private String originalOrderId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Order(OrderId orderId, String orderName, Long memberId, Money finalAmount) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.memberId = memberId;
        this.finalAmount = finalAmount;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Order create(OrderId orderId, String orderName, Long memberId, Money amount) {
        return new Order(orderId, orderName, memberId, amount);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void completePayment(String paymentKey, PaymentMethod paymentMethod) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 완료할 수 없는 주문 상태입니다. 현재 상태: " + this.status);
        }
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.status = OrderStatus.DONE;
    }

    public void completePayment(String paymentKey, PaymentMethod paymentMethod, String originalOrderId) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 완료할 수 없는 주문 상태입니다. 현재 상태: " + this.status);
        }
        this.paymentKey = paymentKey;
        this.paymentMethod = paymentMethod;
        this.originalOrderId = originalOrderId;
        this.status = OrderStatus.DONE;
    }

    public void requestRefund() {
        if (this.status != OrderStatus.DONE) {
            throw new IllegalStateException("환불 가능한 주문이 아닙니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.REFUND_REQUESTED;
    }

    public void completeRefund() {
        if (this.status != OrderStatus.REFUND_REQUESTED) {
            throw new IllegalStateException("환불 완료할 수 없는 주문 상태입니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.REFUNDED;
    }

    public void failRefund() {
        this.status = OrderStatus.REFUND_FAILED;
    }

    public void abort() {
        this.status = OrderStatus.ABORTED;
    }

    public boolean isRefundable() {
        return this.status == OrderStatus.DONE;
    }

    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isDone() {
        return this.status == OrderStatus.DONE;
    }
}

