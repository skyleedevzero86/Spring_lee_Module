package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.payment.application.dto.*;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.ApprovePaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentDetailUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.GetPaymentHistoryUseCase;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.PaymentStatus;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.OrderNo;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.PaymentId;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.TossPaymentClient;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.TossPaymentClientException;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.*;
import com.sleekydz86.payment2v2.global.constants.HeaderConstants;
import com.sleekydz86.payment2v2.global.constants.PaymentConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements ApprovePaymentUseCase, GetPaymentHistoryUseCase, GetPaymentDetailUseCase {

    private static final String LOG_USER_ID = "userId";
    private static final String LOG_USER_ROLE = "userRole";
    private static final String LOG_PAYMENT_ID = "paymentId";
    private static final String LOG_ORDER_NO = "orderNo";
    private static final String LOG_OPERATION = "operation";

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_USER_ID, command.getUserId() != null ? String.valueOf(command.getUserId()) : "unknown",
                LOG_ORDER_NO, command.getOrderNo() != null ? command.getOrderNo() : "unknown",
                LOG_OPERATION, "createPayment"), () -> {
                    log.info("결제 생성 요청");

                    MemberId userId = MemberId.of(command.getUserId());
                    OrderNo orderNo = OrderNo.of(command.getOrderNo());

                    if (paymentRepository.existsByOrderNo(orderNo.getValue())) {
                        log.warn("이미 사용된 주문번호: {}", orderNo.getValue());
                        throw new BusinessException(ErrorCode.DUPLICATE_ORDER_NO);
                    }

                    Payment payment = Payment.builder()
                            .userId(userId.getValue())
                            .orderNo(orderNo.getValue())
                            .productDesc(command.getProductDesc())
                            .amount(command.getAmount())
                            .amountTaxFree(command.getAmountTaxFree())
                            .amountTaxable(command.getAmountTaxable())
                            .amountVat(command.getAmountVat())
                            .amountServiceFee(command.getAmountServiceFee())
                            .disposableCupDeposit(command.getDisposableCupDeposit())
                            .retUrl(command.getRetUrl())
                            .retCancelUrl(command.getRetCancelUrl())
                            .retAppScheme(command.getRetAppScheme())
                            .resultCallback(command.getResultCallback())
                            .callbackVersion(command.getCallbackVersion())
                            .expiredTime(command.getExpiredTime())
                            .build();

                    payment = paymentRepository.save(payment);

                    TossPaymentRequest tossRequest = paymentMapper.toTossRequest(command);
                    TossPaymentResponse tossResponse = callTossPaymentApiOutsideTransaction(tossRequest,
                            orderNo.getValue());

                    return updatePaymentWithTossResponse(payment, tossResponse);
                });
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    private TossPaymentResponse callTossPaymentApiOutsideTransaction(TossPaymentRequest tossRequest, String orderNo) {
        try {
            TossPaymentResponse tossResponse = tossPaymentClient.createPayment(tossRequest);

            if (!tossResponse.isSuccess()) {
                log.error("토스페이먼츠 결제 생성 실패: code={}, msg={}, errorCode={}, orderNo={}",
                        tossResponse.getCode(), tossResponse.getMsg(), tossResponse.getErrorCode(), orderNo);
                throw new BusinessException(ErrorCode.TOSS_PAYMENT_CREATE_FAILED,
                        tossResponse.getMsg() != null ? tossResponse.getMsg() : "결제 생성에 실패했습니다.");
            }

            return tossResponse;
        } catch (TossPaymentClientException e) {
            log.error("토스페이먼츠 API 호출 중 오류 발생: orderNo={}", orderNo, e);
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, e.getMessage(), e);
        }
    }

    @Transactional
    private PaymentResponse updatePaymentWithTossResponse(Payment payment, TossPaymentResponse tossResponse) {
        payment.updateCheckoutInfo(tossResponse.getCheckoutPage(), tossResponse.getPayToken());
        payment = paymentRepository.save(payment);

        log.info("결제 생성 완료: paymentId={}, orderNo={}", payment.getId(), payment.getOrderNoValue());
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentApprovalResponse approvePayment(ApprovePaymentCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_ORDER_NO, command.getOrderNo() != null ? command.getOrderNo() : "unknown",
                LOG_OPERATION, "approvePayment"), () -> {
                    log.info("결제 승인 요청");

                    Payment payment = findPayment(command);
                    validatePaymentStatusForApproval(payment);
                    String payToken = determinePayToken(command, payment);

                    TossPaymentExecuteResponse executeResponse = callTossExecuteApiOutsideTransaction(payToken,
                            command.getOrderNo());

                    return updatePaymentWithApproval(payment, executeResponse);
                });
    }

    private Payment findPayment(ApprovePaymentCommand command) {
        if (command.getPayToken() != null && !command.getPayToken().isBlank()) {
            return paymentRepository.findByPayToken(command.getPayToken())
                    .orElseThrow(() -> {
                        log.warn("결제를 찾을 수 없음: payToken={}", command.getPayToken());
                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("결제 토큰으로 결제 정보를 찾을 수 없습니다. payToken: %s", command.getPayToken()));
                    });
        }

        if (command.getOrderNo() != null && !command.getOrderNo().isBlank()) {
            OrderNo orderNo = OrderNo.of(command.getOrderNo());
            return paymentRepository.findByOrderNo(orderNo.getValue())
                    .orElseThrow(() -> {
                        log.warn("결제를 찾을 수 없음: orderNo={}", orderNo.getValue());
                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("주문번호로 결제 정보를 찾을 수 없습니다. orderNo: %s", orderNo.getValue()));
                    });
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                "결제 토큰 또는 주문번호 중 하나는 필수입니다.");
    }

    private void validatePaymentStatusForApproval(Payment payment) {
        if (PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            log.warn("이미 완료된 결제: paymentId={}, orderNo={}, status={}",
                    payment.getId(), payment.getOrderNoValue(), payment.getStatus());
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_COMPLETED,
                    String.format("이미 완료된 결제입니다. orderNo: %s", payment.getOrderNoValue()));
        }

        if (!PaymentStatus.PENDING.equals(payment.getStatus()) &&
                !PaymentStatus.APPROVED.equals(payment.getStatus())) {
            log.warn("결제 승인 불가 상태: paymentId={}, orderNo={}, status={}",
                    payment.getId(), payment.getOrderNoValue(), payment.getStatus());
            throw new BusinessException(ErrorCode.PAYMENT_NOT_APPROVED,
                    String.format("결제 승인 대기 상태가 아닙니다. 현재 상태: %s, orderNo: %s",
                            payment.getStatus(), payment.getOrderNoValue()));
        }
    }

    private String determinePayToken(ApprovePaymentCommand command, Payment payment) {
        if (command.getPayToken() != null && !command.getPayToken().isBlank()) {
            if (payment.getPayToken() != null && !payment.getPayToken().equals(command.getPayToken())) {
                log.error("결제 토큰 불일치: paymentId={}, orderNo={}, expected={}, actual={}",
                        payment.getId(), payment.getOrderNoValue(), payment.getPayToken(), command.getPayToken());
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        String.format("결제 토큰이 일치하지 않습니다. orderNo: %s", payment.getOrderNoValue()));
            }
            return command.getPayToken();
        }

        if (payment.getPayToken() == null || payment.getPayToken().isBlank()) {
            log.warn("결제 토큰 없음: paymentId={}, orderNo={}", payment.getId(), payment.getOrderNoValue());
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                    String.format("결제 토큰이 없습니다. orderNo: %s", payment.getOrderNoValue()));
        }

        return payment.getPayToken();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    private TossPaymentExecuteResponse callTossExecuteApiOutsideTransaction(String payToken, String orderNo) {
        try {
            TossPaymentExecuteRequest executeRequest = TossPaymentExecuteRequest.builder()
                    .payToken(payToken)
                    .orderNo(orderNo)
                    .build();

            TossPaymentExecuteResponse executeResponse = tossPaymentClient.executePayment(executeRequest);

            if (!executeResponse.isSuccess()) {
                log.error("토스페이먼츠 결제 승인 실패: code={}, msg={}, errorCode={}, orderNo={}",
                        executeResponse.getCode(), executeResponse.getMsg(), executeResponse.getErrorCode(), orderNo);
                throw new BusinessException(ErrorCode.TOSS_PAYMENT_EXECUTE_FAILED,
                        executeResponse.getMsg() != null ? executeResponse.getMsg() : "결제 승인에 실패했습니다.");
            }

            return executeResponse;
        } catch (TossPaymentClientException e) {
            log.error("토스페이먼츠 결제 승인 API 호출 중 오류 발생: orderNo={}", orderNo, e);
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, e.getMessage(), e);
        }
    }

    @Transactional
    private PaymentApprovalResponse updatePaymentWithApproval(Payment payment,
            TossPaymentExecuteResponse executeResponse) {
        updatePaymentWithApprovalData(payment, executeResponse);
        updatePaymentMethodInfo(payment, executeResponse);
        payment = paymentRepository.save(payment);

        log.info("결제 승인 완료: paymentId={}, orderNo={}, transactionId={}",
                payment.getId(), payment.getOrderNoValue(), payment.getTransactionId());

        return paymentMapper.toApprovalResponse(payment);
    }

    private void updatePaymentWithApprovalData(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.approvePayment(
                executeResponse.getMode(),
                executeResponse.getApprovalTime(),
                executeResponse.getStateMsg(),
                executeResponse.getPayMethod(),
                executeResponse.getDiscountedAmount() != null
                        ? BigDecimal.valueOf(executeResponse.getDiscountedAmount())
                        : null,
                executeResponse.getPaidAmount() != null ? BigDecimal.valueOf(executeResponse.getPaidAmount()) : null,
                executeResponse.getTransactionId(),
                executeResponse.getCashReceiptMgtKey());
    }

    private void updatePaymentMethodInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        String payMethod = executeResponse.getPayMethod();
        if (payMethod == null) {
            return;
        }

        switch (payMethod) {
            case PaymentConstants.PAY_METHOD_CARD -> updateCardInfo(payment, executeResponse);
            case PaymentConstants.PAY_METHOD_TOSS_MONEY -> updateAccountInfo(payment, executeResponse);
            default -> log.debug("지원하지 않는 결제 수단: payMethod={}", payMethod);
        }
    }

    private void updateCardInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.updateApprovalCardInfo(
                executeResponse.getCardCompanyName(),
                executeResponse.getCardCompanyCode(),
                executeResponse.getCardAuthorizationNo(),
                executeResponse.getSpreadOut(),
                executeResponse.getNoInterest(),
                executeResponse.getSalesCheckLinkUrl(),
                executeResponse.getCardMethodType(),
                executeResponse.getCardNumber(),
                executeResponse.getCardUserType(),
                executeResponse.getCardBinNumber(),
                executeResponse.getCardNum4Print());
    }

    private void updateAccountInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.updateApprovalAccountInfo(
                executeResponse.getAccountBankCode(),
                executeResponse.getAccountBankName(),
                executeResponse.getAccountNumber());
    }

    public PaymentStatusResponse getPaymentStatus(GetPaymentStatusCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_ORDER_NO, command.getOrderNo() != null ? command.getOrderNo() : "unknown",
                LOG_OPERATION, "getPaymentStatus"), () -> {
                    log.info("결제 상태 확인 요청");

                    try {
                        TossPaymentStatusRequest statusRequest = TossPaymentStatusRequest.builder()
                                .payToken(command.getPayToken())
                                .orderNo(command.getOrderNo())
                                .build();

                        TossPaymentStatusResponse statusResponse = tossPaymentClient.getPaymentStatus(statusRequest);

                        if (!statusResponse.isSuccess()) {
                            log.error("토스페이먼츠 결제 상태 확인 실패: code={}, msg={}, errorCode={}, orderNo={}",
                                    statusResponse.getCode(), statusResponse.getMsg(), statusResponse.getErrorCode(),
                                    command.getOrderNo());
                            throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR,
                                    statusResponse.getMsg() != null ? statusResponse.getMsg() : "결제 상태 확인에 실패했습니다.");
                        }

                        return paymentMapper.toStatusResponse(statusResponse);
                    } catch (TossPaymentClientException e) {
                        log.error("토스페이먼츠 결제 상태 확인 API 호출 중 오류 발생: orderNo={}", command.getOrderNo(), e);
                        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, e.getMessage(), e);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(Long userIdValue, String userRoleValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_USER_ID, userIdValue != null ? String.valueOf(userIdValue) : "unknown",
                LOG_USER_ROLE, userRoleValue != null ? userRoleValue : "unknown",
                LOG_OPERATION, "getPaymentHistory"), () -> {
                    log.info("결제 이력 조회 요청");

                    MemberId userId = MemberId.of(userIdValue);
                    boolean isAdmin = PaymentConstants.ROLE_ADMIN.equalsIgnoreCase(userRoleValue);
                    List<Payment> payments = isAdmin
                            ? paymentRepository.findAllByOrderByCreatedAtDesc()
                            : paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId.getValue());

                    log.debug("조회 결과: {}건", payments.size());
                    return payments.stream()
                            .map(payment -> paymentMapper.toHistoryResponse(payment, isAdmin))
                            .toList();
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetail(Long paymentIdValue, Long userIdValue, String userRoleValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_PAYMENT_ID, paymentIdValue != null ? String.valueOf(paymentIdValue) : "unknown",
                LOG_USER_ID, userIdValue != null ? String.valueOf(userIdValue) : "unknown",
                LOG_USER_ROLE, userRoleValue != null ? userRoleValue : "unknown",
                LOG_OPERATION, "getPaymentDetail"), () -> {
                    log.info("결제 상세 조회 요청");

                    PaymentId paymentId = PaymentId.of(paymentIdValue);
                    MemberId userId = MemberId.of(userIdValue);
                    boolean isAdmin = PaymentConstants.ROLE_ADMIN.equalsIgnoreCase(userRoleValue);

                    Payment payment = isAdmin
                            ? paymentRepository.findById(paymentId.getValue())
                                    .orElseThrow(() -> {
                                        log.warn("결제를 찾을 수 없음: paymentId={}", paymentId.getValue());
                                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                                String.format("결제 정보를 찾을 수 없습니다. paymentId: %d", paymentId.getValue()));
                                    })
                            : paymentRepository.findByIdAndUserId(paymentId.getValue(), userId.getValue())
                                    .orElseThrow(() -> {
                                        log.warn("결제를 찾을 수 없음: paymentId={}, userId={}", paymentId.getValue(),
                                                userId.getValue());
                                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                                String.format("결제 정보를 찾을 수 없습니다. paymentId: %d, userId: %d",
                                                        paymentId.getValue(), userId.getValue()));
                                    });

                    return paymentMapper.toDetailResponse(payment, isAdmin);
                });
    }
}
