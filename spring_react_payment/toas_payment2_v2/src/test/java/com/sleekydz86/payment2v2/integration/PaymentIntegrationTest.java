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
@DisplayName("결제 통합 테스트")
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
        testMember = memberRepository.save(MemberFixture.일반_사용자());
    }

    @Test
    @DisplayName("결제 생성부터 조회까지 전체 플로우가 정상적으로 동작한다")
    void 결제_생성부터_조회까지_전체_플로우가_정상적으로_동작한다() throws Exception {
        // given
        String orderNo = "ORDER-INTEGRATION-001";
        Payment payment = PaymentFixture.기본_결제_생성()
                .orderNo(orderNo)
                .userId(testMember.getId())
                .build();
        payment = paymentRepository.save(payment);

        // when & then - 결제 상세 조회
        mockMvc.perform(get("/api/v1/payments/{paymentId}", payment.getId())
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "USER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()))
                .andExpect(jsonPath("$.orderNo").value(orderNo));
    }

    @Test
    @DisplayName("사용자별 결제 이력 조회가 정상적으로 동작한다")
    void 사용자별_결제_이력_조회가_정상적으로_동작한다() throws Exception {
        // given
        Payment payment1 = PaymentFixture.기본_결제_생성()
                .orderNo("ORDER-001")
                .userId(testMember.getId())
                .build();
        Payment payment2 = PaymentFixture.기본_결제_생성()
                .orderNo("ORDER-002")
                .userId(testMember.getId())
                .build();
        Payment payment3 = PaymentFixture.기본_결제_생성()
                .orderNo("ORDER-003")
                .userId(testMember.getId() + 1)
                .build();

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
        paymentRepository.save(payment3);

        // when & then
        mockMvc.perform(get("/api/v1/payments")
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "USER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("관리자는 모든 결제 이력을 조회할 수 있다")
    void 관리자는_모든_결제_이력을_조회할_수_있다() throws Exception {
        // given
        Payment payment1 = PaymentFixture.기본_결제_생성()
                .orderNo("ORDER-001")
                .userId(testMember.getId())
                .build();
        Payment payment2 = PaymentFixture.기본_결제_생성()
                .orderNo("ORDER-002")
                .userId(testMember.getId() + 1)
                .build();

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // when & then
        mockMvc.perform(get("/api/v1/payments")
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "ADMIN"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("다른 사용자의 결제는 조회할 수 없다")
    void 다른_사용자의_결제는_조회할_수_없다() throws Exception {
        // given
        Member otherMember = memberRepository.save(MemberFixture.이메일로_생성("other@example.com"));
        Payment payment = PaymentFixture.기본_결제_생성()
                .orderNo("ORDER-001")
                .userId(otherMember.getId())
                .build();
        payment = paymentRepository.save(payment);

        // when & then
        mockMvc.perform(get("/api/v1/payments/{paymentId}", payment.getId())
                        .header(HeaderConstants.USER_ID_HEADER, testMember.getId())
                        .header(HeaderConstants.USER_ROLE_HEADER, "USER"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}

