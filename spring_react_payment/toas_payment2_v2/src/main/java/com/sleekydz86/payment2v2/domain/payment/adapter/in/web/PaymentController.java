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
import com.sleekydz86.payment2v2.domain.payment.application.port.in.CancelPaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CancelPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CancelPaymentApiResponse;
import com.sleekydz86.payment2v2.domain.payment.application.service.ReceiptService;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.PaymentStatus;
import com.sleekydz86.payment2v2.global.dto.PageResponse;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.PaymentId;
import com.sleekydz86.payment2v2.global.constants.HeaderConstants;
import com.sleekydz86.payment2v2.global.constants.PaymentConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final PaymentWebMapper paymentWebMapper;
    private final CacheManager cacheManager;
    private final ReceiptService receiptService;
    private final PaymentRepository paymentRepository;

    @PostMapping
    public ResponseEntity<PaymentApiResponse> createPayment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody CreatePaymentRequest request) {
        MemberId memberId = MemberId.of(userId);
        return LoggingUtil.executeWithContext("userId", String.valueOf(memberId.getValue()), () -> {
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

        try {
            PaymentDetailResponse response = getPaymentDetailUseCase.getPaymentDetail(paymentId, userId, userRole);
            PaymentDetailApiResponse apiResponse = paymentWebMapper.toDetailApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        } catch (ClassCastException e) {

            log.warn("캐시 타입 불일치 감지, 캐시 무효화: paymentId={}, userId={}, userRole={}",
                    paymentId, userId, userRole, e);
            String cacheKey = paymentId + "_" + userId + "_" + userRole;
            var cache = cacheManager.getCache("paymentDetail");
            if (cache != null) {
                cache.evict(cacheKey);
                log.info("캐시 무효화 완료: key={}", cacheKey);
            }

            PaymentDetailResponse response = getPaymentDetailUseCase.getPaymentDetail(paymentId, userId, userRole);
            PaymentDetailApiResponse apiResponse = paymentWebMapper.toDetailApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        }
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

    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<CancelPaymentApiResponse> cancelPayment(
            @PathVariable String paymentKey,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody CancelPaymentRequest request) {
        log.info("결제 취소 요청: paymentKey={}, userId={}, cancelReason={}",
                paymentKey, userId, request.getCancelReason());

        CancelPaymentCommand command = paymentWebMapper.toCancelCommand(paymentKey, request);
        CancelPaymentResponse response = cancelPaymentUseCase.cancelPayment(command);
        CancelPaymentApiResponse apiResponse = paymentWebMapper.toCancelApiResponse(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/{paymentId}/cancel-by-id")
    public ResponseEntity<CancelPaymentApiResponse> cancelPaymentById(
            @PathVariable Long paymentId,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @RequestHeader(HeaderConstants.USER_ROLE_HEADER) String userRole,
            @Valid @RequestBody CancelPaymentRequest request) {
        PaymentId.of(paymentId);
        MemberId.of(userId);
        if (userRole == null || userRole.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자 역할이 필요합니다.");
        }

        log.info("결제 취소 요청: paymentId={}, userId={}, cancelReason={}",
                paymentId, userId, request.getCancelReason());

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        Payment payment = isAdmin
                ? paymentRepository.findById(paymentId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("결제 정보를 찾을 수 없습니다. paymentId: %d", paymentId)))
                : paymentRepository.findByIdAndUserId(paymentId, userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("결제 정보를 찾을 수 없습니다. paymentId: %d, userId: %d", paymentId, userId)));

        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "완료된 결제만 취소할 수 있습니다.");
        }

        if (payment.getPaidTs() == null || payment.getPaidTs().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "결제 완료 시간이 없어 취소할 수 없습니다.");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PaymentConstants.DATE_TIME_FORMAT);
            LocalDateTime paidTime = LocalDateTime.parse(payment.getPaidTs(), formatter);
            long daysSincePayment = ChronoUnit.DAYS.between(paidTime, LocalDateTime.now());

            if (daysSincePayment > 15) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "결제일로부터 15일이 지나 취소할 수 없습니다.");
            }
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            log.error("결제 완료 시간 파싱 실패: paidTs={}, paymentId={}", payment.getPaidTs(), paymentId, e);
            throw new BusinessException(ErrorCode.INVALID_DATA_FORMAT,
                    "결제 완료 시간 형식이 올바르지 않습니다.");
        }

        if (payment.getPayToken() == null || payment.getPayToken().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "결제 토큰이 없어 취소할 수 없습니다.");
        }

        String paymentKey = payment.getPayToken();
        CancelPaymentCommand command = paymentWebMapper.toCancelCommand(paymentKey, request);
        CancelPaymentResponse response = cancelPaymentUseCase.cancelPayment(command);
        CancelPaymentApiResponse apiResponse = paymentWebMapper.toCancelApiResponse(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long paymentId,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @RequestHeader(HeaderConstants.USER_ROLE_HEADER) String userRole) {
        PaymentId.of(paymentId);
        MemberId.of(userId);
        if (userRole == null || userRole.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "사용자 역할이 필요합니다.");
        }

        log.info("영수증 다운로드 요청: paymentId={}, userId={}, userRole={}", paymentId, userId, userRole);

        try {
            PaymentDetailResponse paymentDetail = getPaymentDetailUseCase.getPaymentDetail(paymentId, userId, userRole);
            byte[] pdfBytes = receiptService.generateReceiptPdf(paymentDetail);

            String fileName = String.format("영수증_%s_%s.pdf",
                    paymentDetail.getOrderNo(),
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            log.error("영수증 생성 실패: paymentId={}, userId={}", paymentId, userId, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "영수증 생성 중 오류가 발생했습니다.");
        }
    }
}
