package com.sleekydz86.toaspayment.presentation;

import com.sleekydz86.toaspayment.application.dto.PurchaseConfirmRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitResponse;
import com.sleekydz86.toaspayment.application.dto.RefundRequest;
import com.sleekydz86.toaspayment.application.usecase.ConfirmPurchaseUseCase;
import com.sleekydz86.toaspayment.application.usecase.InitPurchaseUseCase;
import com.sleekydz86.toaspayment.application.usecase.RefundOrderUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 Controller 테스트")
class PaymentControllerTest {

    @Mock
    private InitPurchaseUseCase initPurchaseUseCase;

    @Mock
    private ConfirmPurchaseUseCase confirmPurchaseUseCase;

    @Mock
    private RefundOrderUseCase refundOrderUseCase;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    @DisplayName("결제 초기화 성공")
    void initPurchase_success() {
        //given
        PurchaseInitRequest request = new PurchaseInitRequest(1L, 50000);
        PurchaseInitResponse expectedResponse = new PurchaseInitResponse(
                new PurchaseInitResponse.PurchaseInitData("order-uuid-12345")
        );

        when(initPurchaseUseCase.execute(request)).thenReturn(expectedResponse);

        //when
        ResponseEntity<PurchaseInitResponse> response = paymentController.initPurchase(request);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().purchaseUUID()).isEqualTo("order-uuid-12345");
        verify(initPurchaseUseCase, times(1)).execute(request);
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirmPurchase_success() {
        //given
        PurchaseConfirmRequest request = new PurchaseConfirmRequest(
                "payment_key",
                "order_id",
                "예매 티켓",
                50000
        );

        doNothing().when(confirmPurchaseUseCase).execute(request);

        //when
        ResponseEntity<Void> response = paymentController.confirmPurchase(request);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(confirmPurchaseUseCase, times(1)).execute(request);
    }

    @Test
    @DisplayName("환불 성공")
    void refundOrder_success() {
        //given
        RefundRequest request = new RefundRequest(
                "payment_key",
                "order_id",
                "구매자 환불 요청",
                50000
        );

        doNothing().when(refundOrderUseCase).execute(request);

        //when
        ResponseEntity<Void> response = paymentController.refundOrder(request);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(refundOrderUseCase, times(1)).execute(request);
    }
}

