package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.payment.application.dto.GetPaymentStatusCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentDetailResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentHistoryResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentDetailUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentHistoryPageUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentHistoryUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentStatusUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.PaymentId;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.global.constants.PaymentConstants;
import com.sleekydz86.payment2v2.global.dto.PageResponse;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import com.sleekydz86.payment2v2.global.util.TossPaymentExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQueryService implements GetPaymentHistoryUseCase, GetPaymentDetailUseCase,
        GetPaymentHistoryPageUseCase, GetPaymentStatusUseCase {

    private static final String LOG_USER_ID = "userId";
    private static final String LOG_USER_ROLE = "userRole";
    private static final String LOG_PAYMENT_ID = "paymentId";
    private static final String LOG_ORDER_NO = "orderNo";
    private static final String LOG_OPERATION = "operation";

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final com.sleekydz86.payment2v2.domain.payment.application.service.mapper.PaymentResponseMapper paymentResponseMapper;

    @Override
    public PaymentStatusResponse getPaymentStatus(GetPaymentStatusCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_ORDER_NO, command.getOrderNo() != null ? command.getOrderNo() : "알수없음",
                LOG_OPERATION, "getPaymentStatus"), () -> {
                    log.info("결제 상태 확인 요청");

                    TossPaymentStatusRequest statusRequest = TossPaymentStatusRequest.builder()
                            .payToken(command.getPayToken())
                            .orderNo(command.getOrderNo())
                            .build();

                    TossPaymentStatusResponse statusResponse = TossPaymentExceptionHandler.handleTossApiCall(
                            "결제 상태 확인", command.getOrderNo(), () -> {
                                TossPaymentStatusResponse response = paymentGatewayPort.getPaymentStatus(statusRequest);
                                return TossPaymentExceptionHandler.validateTossResponse("결제 상태 확인",
                                        command.getOrderNo(), response,
                                        new TossPaymentExceptionHandler.TossResponseValidator<TossPaymentStatusResponse>() {
                                            @Override
                                            public boolean isSuccess(TossPaymentStatusResponse response) {
                                                return response.isSuccess();
                                            }

                                            @Override
                                            public String getErrorMessage(TossPaymentStatusResponse response) {
                                                return response.getMsg();
                                            }

                                            @Override
                                            public ErrorCode getErrorCode() {
                                                return ErrorCode.TOSS_PAYMENT_API_ERROR;
                                            }
                                        });
                            });

                    return paymentResponseMapper.toStatusResponse(statusResponse);
                });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "paymentHistory", key = "#userIdValue + '_' + #userRoleValue", unless = "#result.isEmpty()")
    public List<PaymentHistoryResponse> getPaymentHistory(Long userIdValue, String userRoleValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_USER_ID, userIdValue != null ? String.valueOf(userIdValue) : "알수없음",
                LOG_USER_ROLE, userRoleValue != null ? userRoleValue : "알수없음",
                LOG_OPERATION, "getPaymentHistory"), () -> {
                    log.info("결제 이력 조회 요청");

                    MemberId userId = MemberId.of(userIdValue);
                    boolean isAdmin = PaymentConstants.ROLE_ADMIN.equalsIgnoreCase(userRoleValue);
                    List<Payment> payments = isAdmin
                            ? paymentRepository.findAllByOrderByCreatedAtDesc()
                            : paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId.getValue());

                    log.debug("조회 결과: {}건", payments.size());
                    return payments.stream()
                            .map(payment -> paymentResponseMapper.toHistoryResponse(payment, isAdmin))
                            .toList();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentHistoryResponse> getPaymentHistory(Long userId, String userRole, Pageable pageable) {
        return getPaymentHistoryPage(userId, userRole, pageable);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentHistoryResponse> getPaymentHistoryPage(Long userIdValue, String userRoleValue,
            Pageable pageable) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_USER_ID, userIdValue != null ? String.valueOf(userIdValue) : "알수없음",
                LOG_USER_ROLE, userRoleValue != null ? userRoleValue : "알수없음",
                LOG_OPERATION, "getPaymentHistoryPage"), () -> {
                    log.info("결제 이력 페이징 조회 요청: page={}, size={}", pageable.getPageNumber(),
                            pageable.getPageSize());

                    MemberId userId = MemberId.of(userIdValue);
                    boolean isAdmin = PaymentConstants.ROLE_ADMIN.equalsIgnoreCase(userRoleValue);
                    Page<Payment> paymentPage = isAdmin
                            ? paymentRepository.findAllByOrderByCreatedAtDesc(pageable)
                            : paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId.getValue(), pageable);

                    List<PaymentHistoryResponse> content = paymentPage.getContent().stream()
                            .map(payment -> paymentResponseMapper.toHistoryResponse(payment, isAdmin))
                            .toList();

                    return PageResponse.<PaymentHistoryResponse>builder()
                            .content(content)
                            .page(paymentPage.getNumber())
                            .size(paymentPage.getSize())
                            .totalElements(paymentPage.getTotalElements())
                            .totalPages(paymentPage.getTotalPages())
                            .hasNext(paymentPage.hasNext())
                            .hasPrevious(paymentPage.hasPrevious())
                            .build();
                });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "paymentDetail", key = "#paymentIdValue + '_' + #userIdValue + '_' + #userRoleValue")
    public PaymentDetailResponse getPaymentDetail(Long paymentIdValue, Long userIdValue, String userRoleValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_PAYMENT_ID, paymentIdValue != null ? String.valueOf(paymentIdValue) : "알수없음",
                LOG_USER_ID, userIdValue != null ? String.valueOf(userIdValue) : "알수없음",
                LOG_USER_ROLE, userRoleValue != null ? userRoleValue : "알수없음",
                LOG_OPERATION, "getPaymentDetail"), () -> {
                    log.info("결제 상세 조회 요청");

                    PaymentId paymentId = PaymentId.of(paymentIdValue);
                    MemberId userId = MemberId.of(userIdValue);
                    boolean isAdmin = PaymentConstants.ROLE_ADMIN.equalsIgnoreCase(userRoleValue);

                    Payment payment = findPayment(paymentId, userId, isAdmin);

                    return paymentResponseMapper.toDetailResponse(payment, isAdmin);
                });
    }

    private Payment findPayment(PaymentId paymentId, MemberId userId, boolean isAdmin) {
        if (isAdmin) {
            return paymentRepository.findById(paymentId.getValue())
                    .orElseThrow(() -> {
                        log.warn("결제를 찾을 수 없음: paymentId={}", paymentId.getValue());
                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("결제 정보를 찾을 수 없습니다. paymentId: %d", paymentId.getValue()));
                    });
        } else {
            return paymentRepository.findByIdAndUserId(paymentId.getValue(), userId.getValue())
                    .orElseThrow(() -> {
                        log.warn("결제를 찾을 수 없음: paymentId={}, userId={}", paymentId.getValue(),
                                userId.getValue());
                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("결제 정보를 찾을 수 없습니다. paymentId: %d, userId: %d",
                                        paymentId.getValue(), userId.getValue()));
                    });
        }
    }
}

