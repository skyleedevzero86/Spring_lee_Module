package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.common.fixture.PaymentFixture;
import com.sleekydz86.payment2v2.common.fixture.TossPaymentFixture;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.event.PaymentCreatedEvent;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCreationService 단위 테스트")
class PaymentCreationServiceTest {

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
    private PaymentCreationService paymentCreationService;

    @Test
    @DisplayName("결제 생성이 성공적으로 완료된다")
    void 결제_생성이_성공적으로_완료된다() {
        // given
        CreatePaymentCommand command = CreatePaymentCommand.builder()
                .userId(1L)
                .orderNo("ORDER-001")
                .productDesc("테스트 상품")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .retUrl("https://example.com/return")
                .retCancelUrl("https://example.com/cancel")
                .expiredTime(LocalDateTime.now().plusHours(1))
                .build();

        Payment savedPayment = PaymentFixture.기본_결제_생성().build();
        ReflectionTestUtils.setField(savedPayment, "id", 1L);

        TossPaymentRequest tossRequest = TossPaymentRequest.builder().build();
        TossPaymentResponse tossResponse = TossPaymentFixture.성공한_결제_생성_응답();
        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(1L)
                .orderNo("ORDER-001")
                .checkoutPage("https://toss.im/checkout/test-token")
                .payToken("test-pay-token-123")
                .build();

        given(paymentRepository.existsByOrderNo("ORDER-001")).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
        given(tossPaymentMapper.toTossRequest(command)).willReturn(tossRequest);
        given(paymentGatewayPort.createPayment(tossRequest)).willReturn(tossResponse);
        given(paymentResponseMapper.toResponse(any(Payment.class))).willReturn(expectedResponse);

        // when
        PaymentResponse result = paymentCreationService.createPayment(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNo()).isEqualTo("ORDER-001");
        assertThat(result.getCheckoutPage()).isEqualTo("https://toss.im/checkout/test-token");
        assertThat(result.getPayToken()).isEqualTo("test-pay-token-123");

        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentGatewayPort, times(1)).createPayment(tossRequest);
        verify(eventPublisher, times(1)).publishEvent(any(PaymentCreatedEvent.class));
        verify(paymentMetricsService, times(1)).recordPaymentCreated();
    }

    @Test
    @DisplayName("중복된 주문번호로 결제 생성 시 예외가 발생한다")
    void 중복된_주문번호로_결제_생성_시_예외가_발생한다() {
        // given
        CreatePaymentCommand command = CreatePaymentCommand.builder()
                .userId(1L)
                .orderNo("ORDER-001")
                .productDesc("테스트 상품")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .retUrl("https://example.com/return")
                .retCancelUrl("https://example.com/cancel")
                .expiredTime(LocalDateTime.now().plusHours(1))
                .build();

        given(paymentRepository.existsByOrderNo("ORDER-001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> paymentCreationService.createPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_ORDER_NO);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentGatewayPort, never()).createPayment(any());
    }

    @Test
    @DisplayName("토스페이먼츠 API 호출 실패 시 예외가 발생한다")
    void 토스페이먼츠_API_호출_실패_시_예외가_발생한다() {
        // given
        CreatePaymentCommand command = CreatePaymentCommand.builder()
                .userId(1L)
                .orderNo("ORDER-001")
                .productDesc("테스트 상품")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .retUrl("https://example.com/return")
                .retCancelUrl("https://example.com/cancel")
                .expiredTime(LocalDateTime.now().plusHours(1))
                .build();

        Payment savedPayment = PaymentFixture.기본_결제_생성().build();
        ReflectionTestUtils.setField(savedPayment, "id", 1L);

        TossPaymentRequest tossRequest = TossPaymentRequest.builder().build();
        TossPaymentResponse tossResponse = TossPaymentFixture.실패한_결제_생성_응답();

        given(paymentRepository.existsByOrderNo("ORDER-001")).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
        given(tossPaymentMapper.toTossRequest(command)).willReturn(tossRequest);
        given(paymentGatewayPort.createPayment(tossRequest)).willReturn(tossResponse);

        // when & then
        assertThatThrownBy(() -> paymentCreationService.createPayment(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TOSS_PAYMENT_CREATE_FAILED);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("결제 생성 시 이벤트가 올바르게 발행된다")
    void 결제_생성_시_이벤트가_올바르게_발행된다() {
        // given
        CreatePaymentCommand command = CreatePaymentCommand.builder()
                .userId(1L)
                .orderNo("ORDER-001")
                .productDesc("테스트 상품")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .retUrl("https://example.com/return")
                .retCancelUrl("https://example.com/cancel")
                .expiredTime(LocalDateTime.now().plusHours(1))
                .build();

        Payment savedPayment = PaymentFixture.기본_결제_생성().build();
        ReflectionTestUtils.setField(savedPayment, "id", 1L);

        TossPaymentRequest tossRequest = TossPaymentRequest.builder().build();
        TossPaymentResponse tossResponse = TossPaymentFixture.성공한_결제_생성_응답();
        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(1L)
                .orderNo("ORDER-001")
                .checkoutPage("https://toss.im/checkout/test-token")
                .payToken("test-pay-token-123")
                .build();

        given(paymentRepository.existsByOrderNo("ORDER-001")).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
        given(tossPaymentMapper.toTossRequest(command)).willReturn(tossRequest);
        given(paymentGatewayPort.createPayment(tossRequest)).willReturn(tossResponse);
        given(paymentResponseMapper.toResponse(any(Payment.class))).willReturn(expectedResponse);

        ArgumentCaptor<PaymentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentCreatedEvent.class);

        // when
        paymentCreationService.createPayment(command);

        // then
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        PaymentCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getPaymentId()).isEqualTo(1L);
        assertThat(capturedEvent.getOrderNo()).isEqualTo("ORDER-001");
        assertThat(capturedEvent.getUserId()).isEqualTo(1L);
        assertThat(capturedEvent.getProductDesc()).isEqualTo("테스트 상품");
    }
}

