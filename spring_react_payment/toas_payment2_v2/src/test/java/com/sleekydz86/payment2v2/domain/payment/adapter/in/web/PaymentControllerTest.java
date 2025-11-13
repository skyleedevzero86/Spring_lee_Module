package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CreatePaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.CreatePaymentUseCase;
import com.sleekydz86.payment2v2.global.constants.HeaderConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController 테스트")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreatePaymentUseCase createPaymentUseCase;

    @MockBean
    private PaymentWebMapper paymentWebMapper;

    @Test
    @DisplayName("결제 생성 API가 성공적으로 동작한다")
    void 결제_생성_API가_성공적으로_동작한다() throws Exception {
        // given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNo("ORDER-001");
        request.setProductDesc("테스트 상품");
        request.setAmount(new BigDecimal("10000"));
        request.setAmountTaxFree(new BigDecimal("0"));
        request.setRetUrl("https://example.com/return");
        request.setRetCancelUrl("https://example.com/cancel");
        request.setExpiredTime(LocalDateTime.now().plusHours(1));

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .orderNo("ORDER-001")
                .checkoutPage("https://toss.im/checkout/test-token")
                .payToken("test-pay-token-123")
                .build();

        given(paymentWebMapper.toCommand(any(), anyLong())).willReturn(null);
        given(createPaymentUseCase.createPayment(any())).willReturn(response);
        given(paymentWebMapper.toApiResponse(response)).willReturn(null);

        // when & then
        mockMvc.perform(post("/api/v1/payments")
                        .header(HeaderConstants.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("결제 생성 API에서 필수 필드 누락 시 400 에러가 발생한다")
    void 결제_생성_API에서_필수_필드_누락_시_400_에러가_발생한다() throws Exception {
        // given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNo("ORDER-001");

        // when & then
        mockMvc.perform(post("/api/v1/payments")
                        .header(HeaderConstants.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결제 생성 API에서 사용자 ID 헤더 누락 시 400 에러가 발생한다")
    void 결제_생성_API에서_사용자_ID_헤더_누락_시_400_에러가_발생한다() throws Exception {
        // given
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderNo("ORDER-001");
        request.setProductDesc("테스트 상품");
        request.setAmount(new BigDecimal("10000"));
        request.setAmountTaxFree(new BigDecimal("0"));
        request.setRetUrl("https://example.com/return");
        request.setRetCancelUrl("https://example.com/cancel");
        request.setExpiredTime(LocalDateTime.now().plusHours(1));

        // when & then
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

