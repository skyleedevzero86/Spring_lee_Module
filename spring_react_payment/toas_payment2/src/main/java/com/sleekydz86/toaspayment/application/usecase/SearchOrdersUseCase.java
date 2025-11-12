package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.OrderResponse;
import com.sleekydz86.toaspayment.application.dto.SearchOrdersRequest;
import com.sleekydz86.toaspayment.application.util.OrderDisplayUtil;
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
public class SearchOrdersUseCase {
    private final OrderRepository orderRepository;

    public List<OrderResponse> execute(SearchOrdersRequest request) {
        List<Order> allOrders = orderRepository.findAll();

        return allOrders.stream()
                .filter(order -> {
                    if (request.orderId() != null && !order.getOrderId().toString().contains(request.orderId())) {
                        return false;
                    }
                    if (request.memberId() != null && !order.getMemberId().equals(request.memberId())) {
                        return false;
                    }
                    if (request.status() != null && !order.getStatus().name().equals(request.status())) {
                        return false;
                    }
                    if (request.startDate() != null && order.getCreatedAt().isBefore(request.startDate())) {
                        return false;
                    }
                    if (request.endDate() != null && order.getCreatedAt().isAfter(request.endDate())) {
                        return false;
                    }
                    return true;
                })
                .map(order -> {
                    String orderIdValue = order.getOrderId().toString();
                    String displayOrderId = OrderDisplayUtil.isOrdersFormat(orderIdValue) 
                        ? orderIdValue 
                        : (OrderDisplayUtil.isUuidFormat(orderIdValue) ? null : orderIdValue);
                    String originalOrderId = order.getOriginalOrderId() != null 
                        ? order.getOriginalOrderId() 
                        : (OrderDisplayUtil.isUuidFormat(orderIdValue) ? orderIdValue : null);
                    
                    return new OrderResponse(
                            order.getId(),
                            displayOrderId,
                            originalOrderId,
                            order.getOrderName(),
                            order.getMemberId(),
                            order.getFinalAmount().toInteger(),
                            order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
                            OrderDisplayUtil.getPaymentMethodDisplayName(order.getPaymentMethod()),
                            order.getPaymentKey(),
                            order.getStatus().name(),
                            OrderDisplayUtil.getStatusDisplayName(order.getStatus()),
                            order.getCreatedAt(),
                            order.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}

