package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.OrderResponse;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserOrdersUseCase {
    private final OrderRepository orderRepository;

    public List<OrderResponse> execute(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new com.sleekydz86.toaspayment.exception.BadRequestException("인증 정보가 없습니다.");
        }

        Long memberId;
        try {
            memberId = Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            log.error("사용자 ID 파싱 실패 - principal name: {}", principal.getName());
            throw new com.sleekydz86.toaspayment.exception.BadRequestException("유효하지 않은 사용자 정보입니다.");
        }

        List<Order> orders = orderRepository.findByMemberId(memberId);

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

