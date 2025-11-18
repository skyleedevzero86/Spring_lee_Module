package com.sleekydz86.toaspayment.infrastructure.persistence;

import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId_Value(String orderId);
    Optional<Order> findByOrderId_ValueAndMemberId(String orderId, Long memberId);
    List<Order> findByMemberId(Long memberId);
}

@Component
@RequiredArgsConstructor
class OrderRepositoryImpl implements OrderRepository {
    private final JpaOrderRepository jpaOrderRepository;

    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }

    @Override
    public Optional<Order> findByOrderId(OrderId orderId) {
        return jpaOrderRepository.findByOrderId_Value(orderId.toString());
    }

    @Override
    public Optional<Order> findByOrderIdAndMemberId(OrderId orderId, Long memberId) {
        return jpaOrderRepository.findByOrderId_ValueAndMemberId(orderId.toString(), memberId);
    }

    @Override
    public List<Order> findByMemberId(Long memberId) {
        return jpaOrderRepository.findByMemberId(memberId);
    }

    @Override
    public List<Order> findAll() {
        return jpaOrderRepository.findAll();
    }
}
