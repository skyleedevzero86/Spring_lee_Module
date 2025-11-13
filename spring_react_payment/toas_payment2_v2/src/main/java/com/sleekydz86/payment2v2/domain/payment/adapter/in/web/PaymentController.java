package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.ApprovePaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CreatePaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.GetPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentApprovalApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentDetailApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentHistoryApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentStatusApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.RefundPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.RefundPaymentApiResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.ApprovePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.GetPaymentStatusCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentDetailResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentHistoryResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.ApprovePaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.CreatePaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentDetailUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentHistoryUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentHistoryPageUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentStatusUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.RefundPaymentUseCase;
import com.sleekydz86.payment2v2.global.dto.PageResponse;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.PaymentId;
import com.sleekydz86.payment2v2.global.constants.HeaderConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final ApprovePaymentUseCase approvePaymentUseCase;
    private final GetPaymentStatusUseCase getPaymentStatusUseCase;
    private final GetPaymentHistoryUseCase getPaymentHistoryUseCase;
    private final GetPaymentHistoryPageUseCase getPaymentHistoryPageUseCase;
    private final GetPaymentDetailUseCase getPaymentDetailUseCase;
    private final RefundPaymentUseCase refundPaymentUseCase;
    private final PaymentWebMapper paymentWebMapper;

    @PostMapping
    public ResponseEntity<PaymentApiResponse> createPayment(
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody CreatePaymentRequest request) {
        return LoggingUtil.executeWithContext("userId", String.valueOf(MemberId.of(userId).getValue()), () -> {
            CreatePaymentCommand command = paymentWebMapper.toCommand(request, userId);
            PaymentResponse response = createPaymentUseCase.createPayment(command);
            PaymentApiResponse apiResponse = paymentWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        });
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
        PaymentStatusResponse response = getPaymentStatusUseCase.getPaymentStatus(command);
        PaymentStatusApiResponse apiResponse = paymentWebMapper.toStatusApiResponse(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<List<PaymentHistoryApiResponse>> getPaymentHistory(
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @RequestHeader(HeaderConstants.USER_ROLE_HEADER) String userRole) {
        MemberId.of(userId);
        if (userRole == null || userRole.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자 역할이 필요합니다.");
        }
        List<PaymentHistoryResponse> responses = getPaymentHistoryUseCase.getPaymentHistory(userId, userRole);
        List<PaymentHistoryApiResponse> apiResponses = responses.stream()
                .map(paymentWebMapper::toHistoryApiResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponses);
    }

    @GetMapping("/page")
    public ResponseEntity<PageApiResponse<PaymentHistoryApiResponse>> getPaymentHistoryPage(
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @RequestHeader(HeaderConstants.USER_ROLE_HEADER) String userRole,
            @PageableDefault(size = 20) Pageable pageable) {
        MemberId.of(userId);
        if (userRole == null || userRole.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자 역할이 필요합니다.");
        }
        PageResponse<PaymentHistoryResponse> pageResponse = getPaymentHistoryPageUseCase.getPaymentHistory(userId, userRole, pageable);
        PageApiResponse<PaymentHistoryApiResponse> apiResponse = paymentWebMapper.toPageApiResponse(pageResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailApiResponse> getPaymentDetail(
            @PathVariable Long paymentId,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @RequestHeader(HeaderConstants.USER_ROLE_HEADER) String userRole) {
        PaymentId.of(paymentId);
        MemberId.of(userId);
        if (userRole == null || userRole.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자 역할이 필요합니다.");
        }
        PaymentDetailResponse response = getPaymentDetailUseCase.getPaymentDetail(paymentId, userId, userRole);
        PaymentDetailApiResponse apiResponse = paymentWebMapper.toDetailApiResponse(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<RefundPaymentApiResponse> refundPayment(
            @PathVariable Long paymentId,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody RefundPaymentRequest request) {
        PaymentId.of(paymentId);
        MemberId.of(userId);
        log.info("결제 환불 요청: paymentId={}, userId={}, refundNo={}", paymentId, userId, request.getRefundNo());
        RefundPaymentCommand command = paymentWebMapper.toRefundCommand(paymentId, request);
        RefundPaymentResponse response = refundPaymentUseCase.refundPayment(command);
        RefundPaymentApiResponse apiResponse = paymentWebMapper.toRefundApiResponse(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}

