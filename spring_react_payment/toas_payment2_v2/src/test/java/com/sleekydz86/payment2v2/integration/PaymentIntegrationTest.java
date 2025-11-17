package com.sleekydz86.payment2v2.integration;

import com.sleekydz86.payment2v2.common.fixture.MemberFixture;
import com.sleekydz86.payment2v2.common.fixture.PaymentFixture;
import com.sleekydz86.payment2v2.domain.member.adapter.out.persistence.MemberJpaRepository;
import com.sleekydz86.payment2v2.domain.member.model.Member;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.persistence.PaymentJpaRepository;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.global.constants.HeaderConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("결제 ?�합 ?�스??)
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Autowired
    private PaymentJpaRepository paymentRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(MemberFixture.?�반_?�용??));
    }

    @Test
    @DisplayName("결제 ?�성부??조회까�? ?�체 ?�로?��? ?�상?�으�??�작?�다")
    void 결제_?�성부??조회까�?_?�체_?�로?��?_?�상?�으�??�작?�다() throws Exception {

        String orderNo = "ORDER-INTEGRATION-001";
        Payment payment = PaymentFixture.기본_결제_?�성()
                .orderNo(orderNo)
                .userId(testMember.getId())
                .build();
        payment = paymentRepository.save(payment);

 & then - 결제 ?�세 조회
        mockMvc.perform(get("/api/v1/payments/{paymentId}", payment.getId())
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "USER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.orderNo").value(orderNo));
    }

    @Test
    @DisplayName("?�용?�별 결제 ?�력 조회가 ?�상?�으�??�작?�다")
    void ?�용?�별_결제_?�력_조회가_?�상?�으�??�작?�다() throws Exception {

        Payment payment1 = PaymentFixture.기본_결제_?�성()
                .orderNo("ORDER-001")
                .userId(testMember.getId())
                .build();
        Payment payment2 = PaymentFixture.기본_결제_?�성()
                .orderNo("ORDER-002")
                .userId(testMember.getId())
                .build();
        Payment payment3 = PaymentFixture.기본_결제_?�성()
                .orderNo("ORDER-003")
                .userId(testMember.getId() + 1)
                .build();

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
        paymentRepository.save(payment3);

 & then
        mockMvc.perform(get("/api/v1/payments")
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "USER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("관리자??모든 결제 ?�력??조회?????�다")
    void 관리자??모든_결제_?�력??조회?????�다() throws Exception {

        Payment payment1 = PaymentFixture.기본_결제_?�성()
                .orderNo("ORDER-001")
                .userId(testMember.getId())
                .build();
        Payment payment2 = PaymentFixture.기본_결제_?�성()
                .orderNo("ORDER-002")
                .userId(testMember.getId() + 1)
                .build();

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

 & then
        mockMvc.perform(get("/api/v1/payments")
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "ADMIN"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("?�른 ?�용?�의 결제??조회?????�다")
    void ?�른_?�용?�의_결제??조회?????�다() throws Exception {

        Member otherMember = memberRepository.save(MemberFixture.?�메?�로_?�성("other@example.com"));
        Payment payment = PaymentFixture.기본_결제_?�성()
                .orderNo("ORDER-001")
                .userId(otherMember.getId())
                .build();
        payment = paymentRepository.save(payment);

 & then
        mockMvc.perform(get("/api/v1/payments/{paymentId}", payment.getId())
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "USER"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

