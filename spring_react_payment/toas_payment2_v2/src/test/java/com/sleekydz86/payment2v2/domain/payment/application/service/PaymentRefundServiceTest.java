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
@DisplayName("PaymentRefundService 단위 테스트")
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
    @DisplayName("환불이 성공적으로 완료된다")
    void 환불이_성공적으로_완료된다() {
        // given
        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(1L)
                .refundNo("REFUND-001")
                .amount(new BigDecimal("10000"))
                .reason("고객 요청")
                .build();

        Payment payment = PaymentFixture.완료된_결제();
        ReflectionTestUtils.setField(payment, "id", 1L);

        TossPaymentRefundRequest refundRequest = TossPaymentRefundRequest.builder().build();
        TossPaymentRefundResponse refundResponse = TossPaymentFixture.성공한_환불_응답();
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

        // when
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
    @DisplayName("환불 불가능한 상태의 결제는 환불할 수 없다")
    void 환불_불가능한_상태의_결제는_환불할_수_없다() {
        // given
        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(1L)
                .refundNo("REFUND-001")
                .amount(new BigDecimal("10000"))
                .build();

        Payment payment = PaymentFixture.대기중인_결제();
        ReflectionTestUtils.setField(payment, "id", 1L);

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentRefundService.refundPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_NOT_REFUNDABLE);

        verify(paymentGatewayPort, never()).refundPayment(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("환불 금액이 결제 금액을 초과하면 예외가 발생한다")
    void 환불_금액이_결제_금액을_초과하면_예외가_발생한다() {
        // given
        RefundPaymentCommand command = RefundPaymentCommand.builder()
                .paymentId(1L)
                .refundNo("REFUND-001")
                .amount(new BigDecimal("20000"))
                .build();

        Payment payment = PaymentFixture.완료된_결제();
        ReflectionTestUtils.setField(payment, "id", 1L);

        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentRefundService.refundPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFUND_AMOUNT_EXCEEDS_REFUNDABLE);

        verify(paymentGatewayPort, never()).refundPayment(any());
    }

    @Test
    @DisplayName("존재하지 않는 결제는 환불할 수 없다")
    void 존재하지_않는_결제는_환불할_수_없다() {
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
}

