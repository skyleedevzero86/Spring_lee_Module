package com.sleekydz86.toaspayment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.toaspayment.application.dto.*;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.OrderStatus;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import com.sleekydz86.toaspayment.domain.payment.PaymentGateway;
import com.sleekydz86.toaspayment.domain.user.UserRepository;
import com.sleekydz86.toaspayment.infrastructure.external.dto.TossPaymentResponse;
import com.sleekydz86.toaspayment.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private PaymentGateway paymentGateway;

    private String token;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "payment@example.com",
                "password123",
                "결제 테스트 사용자"
        );

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        userId = userRepository.findByEmail("payment@example.com")
                .orElseThrow()
                .getId();

        token = jwtTokenProvider.generateToken(userId, "payment@example.com");
    }

    @Test
    @DisplayName("결제 초기화 성공")
    void initPurchase_success() throws Exception {

        PurchaseInitRequest request = new PurchaseInitRequest(1L, 50000);

        mockMvc.perform(post("/api/v1/purchase/init")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchaseUUID").exists());
    }

    @Test
    @DisplayName("인증 없이 결제 초기화 실패")
    void initPurchase_withoutAuth_fail() throws Exception {

        PurchaseInitRequest request = new PurchaseInitRequest(1L, 50000);

        mockMvc.perform(post("/api/v1/purchase/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirmPurchase_success() throws Exception {

        PurchaseInitRequest initRequest = new PurchaseInitRequest(1L, 50000);
        String initResponse = mockMvc.perform(post("/api/v1/purchase/init")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(initResponse)
                .get("data")
                .get("purchaseUUID")
                .asText();

        TossPaymentResponse tossResponse = new TossPaymentResponse(
                "test_mid",
                "2023-12-01",
                "payment_key_123",
                "DONE",
                "transaction_key_123",
                orderId,
                "예매 티켓",
                "2023-12-01T10:00:00",
                "2023-12-01T10:00:01",
                false,
                false,
                null,
                "NORMAL",
                "KR",
                "KRW",
                50000,
                0,
                45455,
                4545,
                0,
                0,
                "카드",
                null
        );

        when(paymentGateway.confirmPayment(anyString(), anyString(), any()))
                .thenReturn(tossResponse);

        PurchaseConfirmRequest confirmRequest = new PurchaseConfirmRequest(
                "payment_key_123",
                orderId,
                "예매 티켓",
                50000
        );

        mockMvc.perform(post("/api/v1/purchase/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk());

        var order = orderRepository.findByOrderId(OrderId.of(orderId)).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DONE);
    }

    @Test
    @DisplayName("존재하지 않는 주문 결제 승인 실패")
    void confirmPurchase_notFoundOrder_fail() throws Exception {

        PurchaseConfirmRequest request = new PurchaseConfirmRequest(
                "payment_key_123",
                "not_exist_order_id",
                "예매 티켓",
                50000
        );

        mockMvc.perform(post("/api/v1/purchase/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("주문을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("금액 불일치 결제 승인 실패")
    void confirmPurchase_amountMismatch_fail() throws Exception {

        PurchaseInitRequest initRequest = new PurchaseInitRequest(1L, 50000);
        String initResponse = mockMvc.perform(post("/api/v1/purchase/init")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(initResponse)
                .get("data")
                .get("purchaseUUID")
                .asText();

        PurchaseConfirmRequest request = new PurchaseConfirmRequest(
                "payment_key_123",
                orderId,
                "예매 티켓",
                30000
        );

        mockMvc.perform(post("/api/v1/purchase/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("결제 금액이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("환불 성공")
    void refundOrder_success() throws Exception {

        PurchaseInitRequest initRequest = new PurchaseInitRequest(1L, 50000);
        String initResponse = mockMvc.perform(post("/api/v1/purchase/init")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(initResponse)
                .get("data")
                .get("purchaseUUID")
                .asText();

        TossPaymentResponse confirmResponse = new TossPaymentResponse(
                "test_mid",
                "2023-12-01",
                "payment_key_123",
                "DONE",
                "transaction_key_123",
                orderId,
                "예매 티켓",
                "2023-12-01T10:00:00",
                "2023-12-01T10:00:01",
                false,
                false,
                null,
                "NORMAL",
                "KR",
                "KRW",
                50000,
                0,
                45455,
                4545,
                0,
                0,
                "카드",
                null
        );

        when(paymentGateway.confirmPayment(anyString(), anyString(), any()))
                .thenReturn(confirmResponse);

        PurchaseConfirmRequest confirmRequest = new PurchaseConfirmRequest(
                "payment_key_123",
                orderId,
                "예매 티켓",
                50000
        );

        mockMvc.perform(post("/api/v1/purchase/confirm")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequest)));

        TossPaymentResponse.CancelDto cancelDto = new TossPaymentResponse.CancelDto(
                "구매자 환불 요청",
                "2023-12-01T11:00:00",
                50000,
                0,
                0,
                50000,
                0,
                0,
                "cancel_transaction_key",
                "cancel_receipt_key",
                "DONE",
                "cancel_request_id"
        );

        TossPaymentResponse refundResponse = new TossPaymentResponse(
                "test_mid",
                "2023-12-01",
                "payment_key_123",
                "CANCELED",
                "transaction_key_123",
                orderId,
                "예매 티켓",
                "2023-12-01T10:00:00",
                "2023-12-01T10:00:01",
                false,
                false,
                null,
                "NORMAL",
                "KR",
                "KRW",
                50000,
                0,
                45455,
                4545,
                0,
                0,
                "카드",
                List.of(cancelDto)
        );

        when(paymentGateway.refundPayment(anyString(), anyString()))
                .thenReturn(refundResponse);

        RefundRequest refundRequest = new RefundRequest(
                "payment_key_123",
                orderId,
                "구매자 환불 요청",
                50000
        );

        mockMvc.perform(post("/api/v1/purchase/refund")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk());

        var order = orderRepository.findByOrderId(OrderId.of(orderId)).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("결제 완료되지 않은 주문 환불 실패")
    void refundOrder_notDoneOrder_fail() throws Exception {

        PurchaseInitRequest initRequest = new PurchaseInitRequest(1L, 50000);
        String initResponse = mockMvc.perform(post("/api/v1/purchase/init")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(initResponse)
                .get("data")
                .get("purchaseUUID")
                .asText();

        RefundRequest refundRequest = new RefundRequest(
                "payment_key_123",
                orderId,
                "구매자 환불 요청",
                50000
        );

        mockMvc.perform(post("/api/v1/purchase/refund")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("환불 가능한 주문이 아닙니다."));
    }
}
