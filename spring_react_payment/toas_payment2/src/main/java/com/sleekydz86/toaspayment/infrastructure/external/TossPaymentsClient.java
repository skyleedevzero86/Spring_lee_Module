package com.sleekydz86.toaspayment.infrastructure.external;

import com.sleekydz86.toaspayment.domain.payment.PaymentGateway;
import com.sleekydz86.toaspayment.global.config.TossPaymentsConfig;
import com.sleekydz86.toaspayment.infrastructure.external.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient implements PaymentGateway {
    private final RestTemplate restTemplate;
    private final TossPaymentsConfig config;

    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Integer amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createHeaders());

        try {
            ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(
                    config.getApproveUrl(),
                    entity,
                    TossPaymentResponse.class);

            if (response.getBody() == null) {
                throw new IllegalStateException("토스 페이먼츠에서 응답을 받지 못했습니다.");
            }

            TossPaymentResponse responseBody = response.getBody();
            log.info("토스 페이먼츠 응답 전체 - paymentKey: {}, orderId: {}, status: {}, method: {}, totalAmount: {}",
                    responseBody.paymentKey(), responseBody.orderId(), responseBody.status(),
                    responseBody.method(), responseBody.totalAmount());

            return responseBody;
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("토스 페이먼츠 결제 승인 4xx 오류 - 상태: {}, 응답: {}", e.getStatusCode(), responseBody);
            throw new TossPaymentException("결제 승인에 실패했습니다: " + e.getMessage(), e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("토스 페이먼츠 결제 승인 5xx 오류 - 상태: {}, 응답: {}", e.getStatusCode(), responseBody);
            throw new TossPaymentException("결제 승인 중 서버 오류가 발생했습니다: " + e.getMessage(), e.getStatusCode().value());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("토스 페이먼츠 결제 승인 네트워크 오류 - {}", e.getMessage());
            throw new TossPaymentException("토스 페이먼츠 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", 503);
        } catch (Exception e) {
            log.error("토스 페이먼츠 결제 승인 예상치 못한 오류 - {}", e.getMessage(), e);
            throw new TossPaymentException("결제 처리 중 오류가 발생했습니다.", 500);
        }
    }

    public TossPaymentResponse refundPayment(String paymentKey, String refundReason) {
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", refundReason);

        HttpHeaders headers = createHeaders();
        headers.set("Idempotency-Key", UUID.randomUUID().toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String refundUrl = config.getRefundUrl().replace("{paymentKey}", paymentKey);

        try {
            ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(
                    refundUrl,
                    entity,
                    TossPaymentResponse.class);

            if (response.getBody() == null) {
                throw new IllegalStateException("토스 페이먼츠에서 환불 응답을 받지 못했습니다.");
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("토스 페이먼츠 환불 4xx 오류 - 상태: {}, 응답: {}", e.getStatusCode(), responseBody);
            throw new TossPaymentException("환불 처리에 실패했습니다: " + e.getMessage(), e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("토스 페이먼츠 환불 5xx 오류 - 상태: {}, 응답: {}", e.getStatusCode(), responseBody);
            throw new TossPaymentException("환불 처리 중 서버 오류가 발생했습니다: " + e.getMessage(), e.getStatusCode().value());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("토스 페이먼츠 환불 네트워크 오류 - {}", e.getMessage());
            throw new TossPaymentException("토스 페이먼츠 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", 503);
        } catch (Exception e) {
            log.error("토스 페이먼츠 환불 예상치 못한 오류 - {}", e.getMessage(), e);
            throw new TossPaymentException("환불 처리 중 오류가 발생했습니다.", 500);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(config.getSecretApiKey(), "");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
