package com.sleekydz86.toaspayment.domain.order;

import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findByOrderId(OrderId orderId);
    Optional<Order> findByOrderIdAndMemberId(OrderId orderId, Long memberId);
}

