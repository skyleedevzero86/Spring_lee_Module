package com.sleekydz86.payment2v2.domain.payment.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment 도메인 모델 테스트")
class PaymentTest {

    @Test
    @DisplayName("결제 생성 시 기본 상태는 PENDING이다")
    void 결제_생성_시_기본_상태는_PENDING이다() {
        // given
        Long userId = 1L;
        String orderNo = "ORDER-001";
        String productDesc = "테스트 상품";
        BigDecimal amount = new BigDecimal("10000");
        BigDecimal amountTaxFree = new BigDecimal("0");
        LocalDateTime expiredTime = LocalDateTime.now().plusHours(1);

        // when
        Payment payment = Payment.builder()
                .userId(userId)
                .orderNo(orderNo)
                .productDesc(productDesc)
                .amount(amount)
                .amountTaxFree(amountTaxFree)
                .retUrl("https://example.com/return")
                .retCancelUrl("https://example.com/cancel")
                .expiredTime(expiredTime)
                .build();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getUserId()).isEqualTo(userId);
        assertThat(payment.getOrderNoValue()).isEqualTo(orderNo);
        assertThat(payment.getAmount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("결제 체크아웃 정보 업데이트가 정상적으로 동작한다")
    void 결제_체크아웃_정보_업데이트가_정상적으로_동작한다() {
        // given
        Payment payment = createPayment();
        String checkoutPage = "https://toss.im/checkout";
        String payToken = "test-pay-token-123";

        // when
        payment.updateCheckoutInfo(checkoutPage, payToken);

        // then
        assertThat(payment.getCheckoutPage()).isEqualTo(checkoutPage);
        assertThat(payment.getPayToken()).isEqualTo(payToken);
    }

    @Test
    @DisplayName("결제 체크아웃 정보 업데이트 시 null 값이면 예외가 발생한다")
    void 결제_체크아웃_정보_업데이트_시_null_값이면_예외가_발생한다() {
        // given
        Payment payment = createPayment();

        // when & then
        assertThatThrownBy(() -> payment.updateCheckoutInfo(null, "token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checkoutPage와 payToken은 필수입니다");

        assertThatThrownBy(() -> payment.updateCheckoutInfo("page", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("checkoutPage와 payToken은 필수입니다");
    }

    @Test
    @DisplayName("결제 완료 처리가 정상적으로 동작한다")
    void 결제_완료_처리가_정상적으로_동작한다() {
        // given
        Payment payment = createPayment();
        String payMethod = "카드";
        BigDecimal discountedAmount = new BigDecimal("0");
        BigDecimal paidAmount = new BigDecimal("10000");
        String paidTs = "20240101120000";
        String transactionId = "TXN-123456";

        // when
        payment.completePayment(payMethod, discountedAmount, paidAmount, paidTs, transactionId);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPayMethod()).isEqualTo(payMethod);
        assertThat(payment.getPaidAmount()).isEqualTo(paidAmount);
        assertThat(payment.getTransactionId()).isEqualTo(transactionId);
    }

    @Test
    @DisplayName("결제 완료 처리 시 필수 값이 null이면 예외가 발생한다")
    void 결제_완료_처리_시_필수_값이_null이면_예외가_발생한다() {
        // given
        Payment payment = createPayment();

        // when & then
        assertThatThrownBy(() -> payment.completePayment(null, BigDecimal.ZERO, BigDecimal.ZERO, "ts", "txn"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("payMethod, paidTs, transactionId는 필수입니다");
    }

    @Test
    @DisplayName("결제 승인이 정상적으로 동작한다")
    void 결제_승인이_정상적으로_동작한다() {
        // given
        Payment payment = createPayment();
        String mode = "NORMAL";
        String approvalTime = "20240101120000";
        String stateMsg = "정상승인";
        String payMethod = "카드";
        BigDecimal discountedAmount = new BigDecimal("0");
        BigDecimal paidAmount = new BigDecimal("10000");
        String transactionId = "TXN-123456";

        // when
        payment.approvePayment(mode, approvalTime, stateMsg, payMethod, discountedAmount, paidAmount, transactionId, null);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getMode()).isEqualTo(mode);
        assertThat(payment.getApprovalTime()).isEqualTo(approvalTime);
        assertThat(payment.getTransactionId()).isEqualTo(transactionId);
    }

    @Test
    @DisplayName("결제 승인 시 필수 값이 null이면 예외가 발생한다")
    void 결제_승인_시_필수_값이_null이면_예외가_발생한다() {
        // given
        Payment payment = createPayment();

        // when & then
        assertThatThrownBy(() -> payment.approvePayment(null, "time", "msg", "method", BigDecimal.ZERO, BigDecimal.ZERO, "txn", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mode, approvalTime, stateMsg, payMethod, transactionId는 필수입니다");
    }

    @Test
    @DisplayName("결제 취소가 정상적으로 동작한다")
    void 결제_취소가_정상적으로_동작한다() {
        // given
        Payment payment = createPayment();

        // when
        payment.cancel();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("만료된 결제인지 확인할 수 있다")
    void 만료된_결제인지_확인할_수_있다() {
        // given
        Payment expiredPayment = Payment.builder()
                .userId(1L)
                .orderNo("ORDER-001")
                .productDesc("테스트")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .retUrl("https://example.com")
                .retCancelUrl("https://example.com")
                .expiredTime(LocalDateTime.now().minusHours(1))
                .build();

        Payment validPayment = Payment.builder()
                .userId(1L)
                .orderNo("ORDER-002")
                .productDesc("테스트")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .retUrl("https://example.com")
                .retCancelUrl("https://example.com")
                .expiredTime(LocalDateTime.now().plusHours(1))
                .build();

        // when
        boolean isExpired1 = expiredPayment.isExpired();
        boolean isExpired2 = validPayment.isExpired();

        // then
        assertThat(isExpired1).isTrue();
        assertThat(isExpired2).isFalse();
    }

    @Test
    @DisplayName("환불 가능한 결제인지 확인할 수 있다")
    void 환불_가능한_결제인지_확인할_수_있다() {
        // given
        Payment completedPayment = createPayment();
        completedPayment.completePayment("카드", BigDecimal.ZERO, new BigDecimal("10000"), "20240101120000", "TXN-123");

        Payment pendingPayment = createPayment();

        // when
        boolean canRefund1 = completedPayment.canRefund();
        boolean canRefund2 = pendingPayment.canRefund();

        // then
        assertThat(canRefund1).isTrue();
        assertThat(canRefund2).isFalse();
    }

    @Test
    @DisplayName("결제 승인 검증 시 이미 완료된 결제면 예외가 발생한다")
    void 결제_승인_검증_시_이미_완료된_결제면_예외가_발생한다() {
        // given
        Payment payment = createPayment();
        payment.completePayment("카드", BigDecimal.ZERO, new BigDecimal("10000"), "20240101120000", "TXN-123");

        // when & then
        assertThatThrownBy(() -> payment.validateForApproval())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 완료된 결제입니다");
    }

    @Test
    @DisplayName("결제 승인 검증 시 PENDING 상태면 통과한다")
    void 결제_승인_검증_시_PENDING_상태면_통과한다() {
        // given
        Payment payment = createPayment();

        // when & then
        payment.validateForApproval();
    }

    @Test
    @DisplayName("결제 토큰 검증이 정상적으로 동작한다")
    void 결제_토큰_검증이_정상적으로_동작한다() {
        // given
        Payment payment = createPayment();
        payment.updateCheckoutInfo("https://toss.im/checkout", "test-token-123");

        // when & then
        payment.validatePayToken("test-token-123");

        assertThatThrownBy(() -> payment.validatePayToken("wrong-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 토큰이 일치하지 않습니다");
    }

    @Test
    @DisplayName("환불 검증 시 완료되지 않은 결제면 예외가 발생한다")
    void 환불_검증_시_완료되지_않은_결제면_예외가_발생한다() {
        // given
        Payment payment = createPayment();

        // when & then
        assertThatThrownBy(() -> payment.validateForRefund())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("환불 가능한 상태가 아닙니다");
    }

    @Test
    @DisplayName("환불 금액 검증이 정상적으로 동작한다")
    void 환불_금액_검증이_정상적으로_동작한다() {
        // given
        Payment payment = createPayment();
        payment.completePayment("카드", BigDecimal.ZERO, new BigDecimal("10000"), "20240101120000", "TXN-123");
        payment.updateCheckoutInfo("https://toss.im/checkout", "test-token");

        // when & then
        payment.validateRefundAmount(new BigDecimal("5000"));

        assertThatThrownBy(() -> payment.validateRefundAmount(new BigDecimal("20000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("환불 요청 금액이 결제 금액을 초과합니다");

        assertThatThrownBy(() -> payment.validateRefundAmount(new BigDecimal("0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("환불 요청 금액은 0보다 커야 합니다");

        assertThatThrownBy(() -> payment.validateRefundAmount(new BigDecimal("-1000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("환불 요청 금액은 0보다 커야 합니다");
    }

    private Payment createPayment() {
        return Payment.builder()
                .userId(1L)
                .orderNo("ORDER-001")
                .productDesc("테스트 상품")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .retUrl("https://example.com/return")
                .retCancelUrl("https://example.com/cancel")
                .expiredTime(LocalDateTime.now().plusHours(1))
                .build();
    }

