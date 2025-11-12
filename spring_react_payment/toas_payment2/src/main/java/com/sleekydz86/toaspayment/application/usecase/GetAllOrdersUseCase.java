package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.OrderResponse;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllOrdersUseCase {
    private final OrderRepository orderRepository;

    public List<OrderResponse> execute() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getOrderId().toString(),
                        order.getOrderName(),
                        order.getMemberId(),
                        order.getFinalAmount().toInteger(),
                        order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
                        order.getStatus().name(),
                        order.getCreatedAt(),
                        order.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}

