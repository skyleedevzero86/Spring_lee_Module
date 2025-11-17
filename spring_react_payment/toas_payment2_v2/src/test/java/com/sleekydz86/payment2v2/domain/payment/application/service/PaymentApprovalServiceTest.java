package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.common.fixture.PaymentFixture;
import com.sleekydz86.payment2v2.common.fixture.TossPaymentFixture;
import com.sleekydz86.payment2v2.domain.payment.application.dto.ApprovePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentApprovalService ?�위 ?�스??)
class PaymentApprovalServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @Mock
    private com.sleekydz86.payment2v2.domain.payment.application.service.mapper.PaymentResponseMapper paymentResponseMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentMetricsService paymentMetricsService;

    @InjectMocks
    private PaymentApprovalService paymentApprovalService;

    @Test
    @DisplayName("결제 ?�인???�공?�으�??�료?�다")
    void 결제_?�인???�공?�으�??�료?�다() {

        // given
        ApprovePaymentCommand command = ApprovePaymentCommand.builder()
                .payToken("test-pay-token-123")
                .orderNo("ORDER-001")
                .build();

        Payment payment = PaymentFixture.?�기중??결제();
        // when
        ReflectionTestUtils.setField(payment, "id", 1L);

        TossPaymentExecuteResponse executeResponse = TossPaymentFixture.?�공??결제_?�인_?�답();
        PaymentApprovalResponse expectedResponse = PaymentApprovalResponse.builder()
                .id(1L)
                .orderNo("ORDER-001")
                .status("COMPLETED")
                .transactionId("TXN-123456")
                .build();

        given(paymentRepository.findByPayToken("test-pay-token-123")).willReturn(Optional.of(payment));
        given(paymentGatewayPort.executePayment(any(TossPaymentExecuteRequest.class))).willReturn(executeResponse);
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);
        given(paymentResponseMapper.toApprovalResponse(any(Payment.class))).willReturn(expectedResponse);


        PaymentApprovalResponse result = paymentApprovalService.approvePayment(command);


        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNo()).isEqualTo("ORDER-001");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getTransactionId()).isEqualTo("TXN-123456");

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentGatewayPort, times(1)).executePayment(any(TossPaymentExecuteRequest.class));
        verify(eventPublisher, times(1)).publishEvent(any());
        verify(paymentMetricsService, times(1)).recordPaymentCompleted();
    }

    @Test
    @DisplayName("?��? ?�료??결제???�인?????�다")
    void ?��?_?�료??결제???�인?????�다() {

        // given
        ApprovePaymentCommand command = ApprovePaymentCommand.builder()
                .payToken("test-pay-token-123")
                .orderNo("ORDER-001")
                .build();

        Payment payment = PaymentFixture.?�료??결제();
        // when
        ReflectionTestUtils.setField(payment, "id", 1L);

        given(paymentRepository.findByPayToken("test-pay-token-123")).willReturn(Optional.of(payment));

        // then
        assertThatThrownBy(() -> paymentApprovalService.approvePayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_APPROVED);

        verify(paymentGatewayPort, never()).executePayment(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("존재?��? ?�는 결제???�인?????�다")
    void 존재?��?_?�는_결제???�인?????�다() {

        // given
        ApprovePaymentCommand command = ApprovePaymentCommand.builder()
                .payToken("invalid-token")
                .orderNo("ORDER-001")
                .build();

        given(paymentRepository.findByPayToken("invalid-token")).willReturn(Optional.empty());
        given(paymentRepository.findByOrderNo("ORDER-001")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentApprovalService.approvePayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);

        verify(paymentGatewayPort, never()).executePayment(any());
    }

