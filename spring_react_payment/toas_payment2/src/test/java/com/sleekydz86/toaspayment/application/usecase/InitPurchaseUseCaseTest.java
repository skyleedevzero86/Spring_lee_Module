package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.PurchaseInitRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitResponse;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 초기화 Use Case 테스트")
class InitPurchaseUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private InitPurchaseUseCase initPurchaseUseCase;

    private PurchaseInitRequest request;

    @BeforeEach
    void setUp() {
        request = new PurchaseInitRequest(1L, 50000);
    }

    @Test
    @DisplayName("정상적인 결제 초기화")
    void initPurchaseSuccess() {
        // given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PurchaseInitResponse response = initPurchaseUseCase.execute(request);

        // then
        assertThat(response.data().purchaseUUID()).isNotNull();
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getFinalAmount().toInteger()).isEqualTo(50000);
    }
}



