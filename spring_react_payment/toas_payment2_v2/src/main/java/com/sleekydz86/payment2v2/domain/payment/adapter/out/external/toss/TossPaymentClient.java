package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss;

import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.global.config.TossPaymentProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient implements PaymentGatewayPort {

    private static final String TOSS_PAYMENT_API_URL = "https://pay.toss.im/api/v2/payments";
    private static final String TOSS_PAYMENT_EXECUTE_API_URL = "https://pay.toss.im/api/v2/execute";
    private static final String TOSS_PAYMENT_STATUS_API_URL = "https://pay.toss.im/api/v2/status";
    private static final String TOSS_PAYMENT_REFUND_API_URL = "https://pay.toss.im/api/v2/refunds";

    private final RestTemplate restTemplate;
    private final TossPaymentProperties tossPaymentProperties;
    private final MeterRegistry meterRegistry;

    private String getApiKey() {
        return tossPaymentProperties.getApi().getKey();
    }

    @Override
    public TossPaymentResponse createPayment(TossPaymentRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossPaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(
                    TOSS_PAYMENT_API_URL,
                    entity,
                    TossPaymentResponse.class
            );

            TossPaymentResponse body = response.getBody();
            if (body == null) {
                log.error("토스페이먼츠 결제 생성 API 응답이 null입니다.");
                meterRegistry.counter("toss.payment.api.error", "api", "createPayment").increment();
                throw new TossPaymentClientException("토스페이먼츠 결제 생성 API 응답이 null입니다.");
            }

            long duration = System.currentTimeMillis() - startTime;
            meterRegistry.timer("toss.payment.api.call", "api", "createPayment").record(duration, TimeUnit.MILLISECONDS);

            return body;
        } catch (RestClientException e) {
            log.error("토스페이먼츠 API 호출 중 오류 발생", e);
            meterRegistry.counter("toss.payment.api.error", "api", "createPayment").increment();
            throw new TossPaymentClientException("토스페이먼츠 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public TossPaymentExecuteResponse executePayment(TossPaymentExecuteRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            TossPaymentExecuteRequest requestWithApiKey = TossPaymentExecuteRequest.builder()
                    .apiKey(getApiKey())
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

            TossPaymentExecuteResponse body = response.getBody();
            if (body == null) {
                log.error("토스페이먼츠 결제 승인 API 응답이 null입니다.");
                meterRegistry.counter("toss.payment.api.error", "api", "executePayment").increment();
                throw new TossPaymentClientException("토스페이먼츠 결제 승인 API 응답이 null입니다.");
            }

            long duration = System.currentTimeMillis() - startTime;
            meterRegistry.timer("toss.payment.api.call", "api", "executePayment").record(duration, TimeUnit.MILLISECONDS);

            return body;
        } catch (RestClientException e) {
            log.error("토스페이먼츠 결제 승인 API 호출 중 오류 발생", e);
            meterRegistry.counter("toss.payment.api.error", "api", "executePayment").increment();
            throw new TossPaymentClientException("토스페이먼츠 결제 승인 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public TossPaymentStatusResponse getPaymentStatus(TossPaymentStatusRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            TossPaymentStatusRequest requestWithApiKey = TossPaymentStatusRequest.builder()
                    .apiKey(getApiKey())
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

            TossPaymentStatusResponse body = response.getBody();
            if (body == null) {
                log.error("토스페이먼츠 결제 상태 확인 API 응답이 null입니다.");
                meterRegistry.counter("toss.payment.api.error", "api", "getPaymentStatus").increment();
                throw new TossPaymentClientException("토스페이먼츠 결제 상태 확인 API 응답이 null입니다.");
            }

            long duration = System.currentTimeMillis() - startTime;
            meterRegistry.timer("toss.payment.api.call", "api", "getPaymentStatus").record(duration, TimeUnit.MILLISECONDS);

            return body;
        } catch (RestClientException e) {
            log.error("토스페이먼츠 결제 상태 확인 API 호출 중 오류 발생", e);
            meterRegistry.counter("toss.payment.api.error", "api", "getPaymentStatus").increment();
            throw new TossPaymentClientException("토스페이먼츠 결제 상태 확인 API 호출 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public TossPaymentRefundResponse refundPayment(TossPaymentRefundRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            TossPaymentRefundRequest requestWithApiKey = TossPaymentRefundRequest.builder()
                    .apiKey(getApiKey())
                    .payToken(request.getPayToken())
                    .refundNo(request.getRefundNo())
                    .idempotent(request.getIdempotent())
                    .reason(request.getReason())
                    .amount(request.getAmount())
                    .amountTaxFree(request.getAmountTaxFree())
                    .amountTaxable(request.getAmountTaxable())
                    .amountVat(request.getAmountVat())
                    .amountServiceFee(request.getAmountServiceFee())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TossPaymentRefundRequest> entity = new HttpEntity<>(requestWithApiKey, headers);

            ResponseEntity<TossPaymentRefundResponse> response = restTemplate.postForEntity(
                    TOSS_PAYMENT_REFUND_API_URL,
                    entity,
                    TossPaymentRefundResponse.class
            );

            TossPaymentRefundResponse body = response.getBody();
            if (body == null) {
                log.error("토스페이먼츠 결제 환불 API 응답이 null입니다.");
                meterRegistry.counter("toss.payment.api.error", "api", "refundPayment").increment();
                throw new TossPaymentClientException("토스페이먼츠 결제 환불 API 응답이 null입니다.");
            }

            long duration = System.currentTimeMillis() - startTime;
            meterRegistry.timer("toss.payment.api.call", "api", "refundPayment").record(duration, TimeUnit.MILLISECONDS);

            return body;
        } catch (RestClientException e) {
            log.error("토스페이먼츠 결제 환불 API 호출 중 오류 발생", e);
            meterRegistry.counter("toss.payment.api.error", "api", "refundPayment").increment();
            throw new TossPaymentClientException("토스페이먼츠 결제 환불 API 호출 중 오류가 발생했습니다.", e);
        }
    }
}
