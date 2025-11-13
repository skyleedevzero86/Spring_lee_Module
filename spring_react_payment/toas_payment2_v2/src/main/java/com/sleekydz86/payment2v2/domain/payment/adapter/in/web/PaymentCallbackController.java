package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentCallbackCommand;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.ProcessPaymentCallbackUseCase;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCallbackRequest;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/callback")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final ProcessPaymentCallbackUseCase processPaymentCallbackUseCase;
    private final PaymentCallbackWebMapper paymentCallbackWebMapper;

    @PostMapping
    public ResponseEntity<Void> handleCallback(@Valid @RequestBody TossPaymentCallbackRequest callbackRequest) {
        log.info("결제 콜백 수신: orderNo={}, status={}, payToken={}",
                callbackRequest.getOrderNo(), callbackRequest.getStatus(), callbackRequest.getPayToken());

        try {
            PaymentCallbackCommand command = paymentCallbackWebMapper.toCommand(callbackRequest);
            processPaymentCallbackUseCase.processCallback(command);
            log.info("결제 콜백 처리 성공: orderNo={}", callbackRequest.getOrderNo());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (BusinessException e) {
            log.error("결제 콜백 처리 중 비즈니스 예외 발생: orderNo={}, errorCode={}, message={}",
                    callbackRequest.getOrderNo(), e.getErrorCode().getCode(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            log.error("결제 콜백 처리 중 예상치 못한 오류 발생: orderNo={}",
                    callbackRequest.getOrderNo(), e);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }
}

