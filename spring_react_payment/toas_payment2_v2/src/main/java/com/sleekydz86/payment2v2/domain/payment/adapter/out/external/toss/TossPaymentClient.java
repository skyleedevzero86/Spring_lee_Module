package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss;

import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusResponse;
import com.sleekydz86.payment2v2.global.config.TossPaymentProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class TossPaymentClient {

    private static final String TOSS_PAYMENT_API_URL = "https://pay.toss.im/api/v2/payments";
    private static final String TOSS_PAYMENT_EXECUTE_API_URL = "https://pay.toss.im/api/v2/execute";
    private static final String TOSS_PAYMENT_STATUS_API_URL = "https://pay.toss.im/api/v2/status";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public TossPaymentClient(
            RestTemplate restTemplate,
            TossPaymentProperties tossPaymentProperties) {
        this.restTemplate = restTemplate;
        this.apiKey = tossPaymentProperties.getApi().getKey();
    }

    public TossPaymentResponse createPayment(TossPaymentRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossPaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(
                    TOSS_PAYMENT_API_URL,
                    entity,
                    TossPaymentResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new TossPaymentClientException("토스페이먼츠 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    public TossPaymentExecuteResponse executePayment(TossPaymentExecuteRequest request) {
        try {
            TossPaymentExecuteRequest requestWithApiKey = TossPaymentExecuteRequest.builder()
                    .apiKey(apiKey)
                    .payToken(request.getPayToken())
                    .orderNo(request.getOrderNo())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossPaymentExecuteRequest> entity = new HttpEntity<>(requestWithApiKey, headers);

            ResponseEntity<TossPaymentExecuteResponse> response = restTemplate.postForEntity(
                    TOSS_PAYMENT_EXECUTE_API_URL,
                    entity,
                    TossPaymentExecuteResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new TossPaymentClientException("토스페이먼츠 결제 승인 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    public TossPaymentStatusResponse getPaymentStatus(TossPaymentStatusRequest request) {
        try {
            TossPaymentStatusRequest requestWithApiKey = TossPaymentStatusRequest.builder()
                    .apiKey(apiKey)
                    .payToken(request.getPayToken())
                    .orderNo(request.getOrderNo())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossPaymentStatusRequest> entity = new HttpEntity<>(requestWithApiKey, headers);

            ResponseEntity<TossPaymentStatusResponse> response = restTemplate.postForEntity(
                    TOSS_PAYMENT_STATUS_API_URL,
                    entity,
                    TossPaymentStatusResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new TossPaymentClientException("토스페이먼츠 결제 상태 확인 API 호출 중 오류가 발생했습니다.", e);
        }
    }
}

