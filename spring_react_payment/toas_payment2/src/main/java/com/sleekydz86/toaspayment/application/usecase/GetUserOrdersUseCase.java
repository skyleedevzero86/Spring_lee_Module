package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.OrderResponse;
import com.sleekydz86.toaspayment.application.util.OrderDisplayUtil;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

        boolean isAdmin = isAdminUser();
        List<Order> orders = orderRepository.findByMemberId(memberId);

        return orders.stream()
                .map(order -> {
                    String orderIdValue = order.getOrderId().toString();
                    String displayOrderId;
                    String originalOrderId;
                    
                    if (OrderDisplayUtil.isOrdersFormat(orderIdValue)) {
                        displayOrderId = orderIdValue;
                        originalOrderId = order.getOriginalOrderId();
                    } else if (OrderDisplayUtil.isUuidFormat(orderIdValue)) {
                        if (isAdmin) {
                            displayOrderId = null;
                            originalOrderId = order.getOriginalOrderId() != null 
                                ? order.getOriginalOrderId() 
                                : orderIdValue;
                        } else {
                            displayOrderId = null;
                            originalOrderId = null;
                        }
                    } else {
                        displayOrderId = orderIdValue;
                        originalOrderId = order.getOriginalOrderId();
                    }
                    
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
    
    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }
}

