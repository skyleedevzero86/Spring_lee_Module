package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.common.fixture.PaymentFixture;
import com.sleekydz86.payment2v2.common.fixture.TossPaymentFixture;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundResponse;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRefundService ?�위 ?�스??)
class PaymentRefundServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @Mock
    private com.sleekydz86.payment2v2.domain.payment.application.service.mapper.TossPaymentMapper tossPaymentMapper;

    @Mock
    private com.sleekydz86.payment2v2.domain.payment.application.service.mapper.PaymentResponseMapper paymentResponseMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentMetricsService paymentMetricsService;

    @InjectMocks
    private PaymentRefundService paymentRefundService;

    @Test
    @DisplayName("?�불???�공?�으�??�료?�다")
    void ?�불???�공?�으�??�료?�다() {

        // given
        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(1L)
                .refundNo("REFUND-001")
                .amount(new BigDecimal("10000"))
                .reason("고객 ?�청")
                .build();

        Payment payment = PaymentFixture.?�료??결제();
        // when
        ReflectionTestUtils.setField(payment, "id", 1L);

        TossPaymentRefundRequest refundRequest = TossPaymentRefundRequest.builder().build();
        TossPaymentRefundResponse refundResponse = TossPaymentFixture.?�공???�불_?�답();
        RefundPaymentResponse expectedResponse = RefundPaymentResponse.builder()
                .paymentId(1L)
                .refundNo("REFUND-001")
                .refundedAmount(10000)
                .build();

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
        given(tossPaymentMapper.toRefundRequest(command, payment)).willReturn(refundRequest);
        given(paymentGatewayPort.refundPayment(refundRequest)).willReturn(refundResponse);
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);
        given(paymentResponseMapper.toRefundResponse(any(Payment.class), any(TossPaymentRefundResponse.class)))
                .willReturn(expectedResponse);


        RefundPaymentResponse result = paymentRefundService.refundPayment(command);


        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getRefundNo()).isEqualTo("REFUND-001");
        assertThat(result.getRefundedAmount()).isEqualTo(10000);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentGatewayPort, times(1)).refundPayment(refundRequest);
        verify(eventPublisher, times(1)).publishEvent(any());
        verify(paymentMetricsService, times(1)).recordPaymentRefunded();
    }

    @Test
    @DisplayName("?�불 불�??�한 ?�태??결제???�불?????�다")
    void ?�불_불�??�한_?�태??결제???�불?????�다() {

        // given
        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(1L)
                .refundNo("REFUND-001")
                .amount(new BigDecimal("10000"))
                .build();

        Payment payment = PaymentFixture.?�기중??결제();
        // when
        ReflectionTestUtils.setField(payment, "id", 1L);

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        // then
        assertThatThrownBy(() -> paymentRefundService.refundPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_REFUNDABLE);

        verify(paymentGatewayPort, never()).refundPayment(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("?�불 금액??결제 금액??초과?�면 ?�외가 발생?�다")
    void ?�불_금액??결제_금액??초과?�면_?�외가_발생?�다() {

        // given
        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(1L)
                .refundNo("REFUND-001")
                .amount(new BigDecimal("20000"))
                .build();

        Payment payment = PaymentFixture.?�료??결제();
        // when
        ReflectionTestUtils.setField(payment, "id", 1L);

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        // then
        assertThatThrownBy(() -> paymentRefundService.refundPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFUND_AMOUNT_EXCEEDS_REFUNDABLE);

        verify(paymentGatewayPort, never()).refundPayment(any());
    }

    @Test
    @DisplayName("존재?��? ?�는 결제???�불?????�다")
    void 존재?��?_?�는_결제???�불?????�다() {

        // given
        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(999L)
                .refundNo("REFUND-001")
                .amount(new BigDecimal("10000"))
                .build();

        given(paymentRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentRefundService.refundPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);

        verify(paymentGatewayPort, never()).refundPayment(any());
    }



