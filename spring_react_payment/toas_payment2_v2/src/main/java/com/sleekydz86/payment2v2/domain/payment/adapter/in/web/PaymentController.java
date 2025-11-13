package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.ApprovePaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CreatePaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.GetPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentApprovalApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentStatusApiResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.ApprovePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.GetPaymentStatusCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.ApprovePaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ApprovePaymentUseCase approvePaymentUseCase;
    private final PaymentWebMapper paymentWebMapper;

    @PostMapping
    public ResponseEntity<PaymentApiResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("결제 생성 요청: orderNo={}", request.getOrderNo());
        CreatePaymentCommand command = paymentWebMapper.toCommand(request);
        PaymentResponse response = paymentService.createPayment(command);
        PaymentApiResponse apiResponse = paymentWebMapper.toApiResponse(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/approve")
    public ResponseEntity<PaymentApprovalApiResponse> approvePayment(
            @Valid @RequestBody ApprovePaymentRequest request) {
        log.info("결제 승인 요청: payToken={}, orderNo={}", request.getPayToken(), request.getOrderNo());
        ApprovePaymentCommand command = paymentWebMapper.toApproveCommand(request);
        PaymentApprovalResponse response = approvePaymentUseCase.approvePayment(command);
        PaymentApprovalApiResponse apiResponse = paymentWebMapper.toApprovalApiResponse(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/status")
    public ResponseEntity<PaymentStatusApiResponse> getPaymentStatus(
            @Valid @RequestBody GetPaymentStatusRequest request) {
        log.info("결제 상태 확인 요청: payToken={}, orderNo={}", request.getPayToken(), request.getOrderNo());
        GetPaymentStatusCommand command = paymentWebMapper.toStatusCommand(request);
        PaymentStatusResponse response = paymentService.getPaymentStatus(command);
        PaymentStatusApiResponse apiResponse = paymentWebMapper.toStatusApiResponse(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}

