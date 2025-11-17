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
@DisplayName("PaymentRepository ?�합 ?�스??)
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentJpaRepository paymentRepository;

    @Test
    @DisplayName("결제�??�?�하�?조회?????�다")
    void 결제�??�?�하�?조회?????�다() {

        // given
        Payment payment = PaymentFixture.기본_결제_?�성().build();
        payment = entityManager.persistAndFlush(payment);


        Optional<Payment> found = paymentRepository.findById(payment.getId());


        assertThat(found).isPresent();
        assertThat(found.get().getOrderNoValue()).isEqualTo("ORDER-001");
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("주문번호�?결제�?조회?????�다")
    void 주문번호�?결제�?조회?????�다() {

        // given
        Payment payment = PaymentFixture.기본_결제_?�성().build();
        // when
        entityManager.persistAndFlush(payment);


        Optional<Payment> found = paymentRepository.findByOrderNo("ORDER-001");


        // then
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNoValue()).isEqualTo("ORDER-001");
    }

    @Test
    @DisplayName("주문번호 중복 ?�인???�상?�으�??�작?�다")
    void 주문번호_중복_?�인???�상?�으�??�작?�다() {

        // given
        Payment payment = PaymentFixture.기본_결제_?�성().build();
        // when
        entityManager.persistAndFlush(payment);


        boolean exists = paymentRepository.existsByOrderNo("ORDER-001");
        boolean notExists = paymentRepository.existsByOrderNo("ORDER-002");


        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("?�용??ID�?결제 목록??조회?????�다")
    void ?�용??ID�?결제_목록??조회?????�다() {

        // given
        Payment payment1 = PaymentFixture.기본_결제_?�성().build();
        Payment payment2 = PaymentFixture.기본_결제_?�성()
                .orderNo("ORDER-002")
                .build();
        Payment payment3 = PaymentFixture.기본_결제_?�성()
                .userId(2L)
                .orderNo("ORDER-003")
                .build();

        // when
        entityManager.persistAndFlush(payment1);
        entityManager.persistAndFlush(payment2);
        entityManager.persistAndFlush(payment3);


        List<Payment> payments = paymentRepository.findAllByUserIdOrderByCreatedAtDesc(1L);


        // then
        assertThat(payments).hasSize(2);
        assertThat(payments.get(0).getOrderNoValue()).isEqualTo("ORDER-002");
        assertThat(payments.get(1).getOrderNoValue()).isEqualTo("ORDER-001");
    }

    @Test
    @DisplayName("?�용??ID�??�이�?조회가 ?�상?�으�??�작?�다")
    void ?�용??ID�??�이�?조회가_?�상?�으�??�작?�다() {

        for (int i = 1; i <= 25; i++) {
            // given
            Payment payment = PaymentFixture.기본_결제_?�성()
                    .orderNo("ORDER-" + String.format("%03d", i))
                    .build();
            // when
            entityManager.persistAndFlush(payment);
        // then
        }

        Pageable pageable = PageRequest.of(0, 10);


        Page<Payment> paymentPage = paymentRepository.findAllByUserIdOrderByCreatedAtDesc(1L, pageable);


        assertThat(paymentPage.getContent()).hasSize(10);
        assertThat(paymentPage.getTotalElements()).isEqualTo(25);
        assertThat(paymentPage.getTotalPages()).isEqualTo(3);
        assertThat(paymentPage.hasNext()).isTrue();
    }

    @Test
    @DisplayName("결제 ?�큰?�로 결제�?조회?????�다")
    void 결제_?�큰?�로_결제�?조회?????�다() {

        // given
        Payment payment = PaymentFixture.?�기중??결제();
        // when
        entityManager.persistAndFlush(payment);


        Optional<Payment> found = paymentRepository.findByPayToken("test-pay-token-456");


        // then
        assertThat(found).isPresent();
        assertThat(found.get().getPayToken()).isEqualTo("test-pay-token-456");
    }

    @Test
    @DisplayName("?�용??ID?� 결제 ID�?결제�?조회?????�다")
    void ?�용??ID?�_결제_ID�?결제�?조회?????�다() {

        // given
        Payment payment = PaymentFixture.기본_결제_?�성().build();
        // when
        entityManager.persistAndFlush(payment);


        Optional<Payment> found = paymentRepository.findByIdAndUserId(payment.getId(), 1L);


        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(payment.getId());
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("?�른 ?�용?�의 결제??조회?��? ?�는??)
    void ?�른_?�용?�의_결제??조회?��?_?�는??) {

        Payment payment = PaymentFixture.기본_결제_?�성()
                .userId(1L)
                .build();
        entityManager.persistAndFlush(payment);


        Optional<Payment> found = paymentRepository.findByIdAndUserId(payment.getId(), 2L);


        assertThat(found).isEmpty();
    }

