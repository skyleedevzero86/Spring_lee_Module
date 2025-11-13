package com.sleekydz86.payment2v2.domain.payment.adapter.out.persistence;

import com.sleekydz86.payment2v2.common.fixture.PaymentFixture;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PaymentRepository 통합 테스트")
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentJpaRepository paymentRepository;

    @Test
    @DisplayName("결제를 저장하고 조회할 수 있다")
    void 결제를_저장하고_조회할_수_있다() {
        // given
        Payment payment = PaymentFixture.기본_결제_생성().build();
        payment = entityManager.persistAndFlush(payment);

        // when
        Optional<Payment> found = paymentRepository.findById(payment.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNoValue()).isEqualTo("ORDER-001");
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("주문번호로 결제를 조회할 수 있다")
    void 주문번호로_결제를_조회할_수_있다() {
        // given
        Payment payment = PaymentFixture.기본_결제_생성().build();
        entityManager.persistAndFlush(payment);

        // when
        Optional<Payment> found = paymentRepository.findByOrderNo("ORDER-001");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNoValue()).isEqualTo("ORDER-001");
    }

    @Test
    @DisplayName("주문번호 중복 확인이 정상적으로 동작한다")
    void 주문번호_중복_확인이_정상적으로_동작한다() {
        // given
        Payment payment = PaymentFixture.기본_결제_생성().build();
        entityManager.persistAndFlush(payment);

        // when
        boolean exists = paymentRepository.existsByOrderNo("ORDER-001");
        boolean notExists = paymentRepository.existsByOrderNo("ORDER-002");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사용자 ID로 결제 목록을 조회할 수 있다")
    void 사용자_ID로_결제_목록을_조회할_수_있다() {
        // given
        Payment payment1 = PaymentFixture.기본_결제_생성().build();
        Payment payment2 = PaymentFixture.기본_결제_생성()
                .orderNo("ORDER-002")
                .build();
        Payment payment3 = PaymentFixture.기본_결제_생성()
                .userId(2L)
                .orderNo("ORDER-003")
                .build();

        entityManager.persistAndFlush(payment1);
        entityManager.persistAndFlush(payment2);
        entityManager.persistAndFlush(payment3);

        // when
        List<Payment> payments = paymentRepository.findAllByUserIdOrderByCreatedAtDesc(1L);

        // then
        assertThat(payments).hasSize(2);
        assertThat(payments.get(0).getOrderNoValue()).isEqualTo("ORDER-002");
        assertThat(payments.get(1).getOrderNoValue()).isEqualTo("ORDER-001");
    }

    @Test
    @DisplayName("사용자 ID로 페이징 조회가 정상적으로 동작한다")
    void 사용자_ID로_페이징_조회가_정상적으로_동작한다() {
        // given
        for (int i = 1; i <= 25; i++) {
            Payment payment = PaymentFixture.기본_결제_생성()
                    .orderNo("ORDER-" + String.format("%03d", i))
                    .build();
            entityManager.persistAndFlush(payment);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Payment> paymentPage = paymentRepository.findAllByUserIdOrderByCreatedAtDesc(1L, pageable);

        // then
        assertThat(paymentPage.getContent()).hasSize(10);
        assertThat(paymentPage.getTotalElements()).isEqualTo(25);
        assertThat(paymentPage.getTotalPages()).isEqualTo(3);
        assertThat(paymentPage.hasNext()).isTrue();
    }

    @Test
    @DisplayName("결제 토큰으로 결제를 조회할 수 있다")
    void 결제_토큰으로_결제를_조회할_수_있다() {
        // given
        Payment payment = PaymentFixture.대기중인_결제();
        entityManager.persistAndFlush(payment);

        // when
        Optional<Payment> found = paymentRepository.findByPayToken("test-pay-token-456");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getPayToken()).isEqualTo("test-pay-token-456");
    }

    @Test
    @DisplayName("사용자 ID와 결제 ID로 결제를 조회할 수 있다")
    void 사용자_ID와_결제_ID로_결제를_조회할_수_있다() {
        // given
        Payment payment = PaymentFixture.기본_결제_생성().build();
        entityManager.persistAndFlush(payment);

        // when
        Optional<Payment> found = paymentRepository.findByIdAndUserId(payment.getId(), 1L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(payment.getId());
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("다른 사용자의 결제는 조회되지 않는다")
    void 다른_사용자의_결제는_조회되지_않는다() {
        // given
        Payment payment = PaymentFixture.기본_결제_생성()
                .userId(1L)
                .build();
        entityManager.persistAndFlush(payment);

        // when
        Optional<Payment> found = paymentRepository.findByIdAndUserId(payment.getId(), 2L);

        // then
        assertThat(found).isEmpty();
    }
}

