package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.dto.ApprovePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.GetPaymentStatusCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.ApprovePaymentUseCase;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.TossPaymentClient;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.TossPaymentClientException;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.PaymentStatus;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService implements ApprovePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentMapper paymentMapper;

    public PaymentResponse createPayment(CreatePaymentCommand command) {
        if (paymentRepository.existsByOrderNo(command.getOrderNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER_NO);
        }

        Payment payment = Payment.builder()
                .orderNo(command.getOrderNo())
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

        try {
            TossPaymentRequest tossRequest = paymentMapper.toTossRequest(command);
            TossPaymentResponse tossResponse = tossPaymentClient.createPayment(tossRequest);

            if (!tossResponse.isSuccess()) {
                log.error("토스페이먼츠 결제 생성 실패: code={}, msg={}, errorCode={}",
                        tossResponse.getCode(), tossResponse.getMsg(), tossResponse.getErrorCode());
                throw new BusinessException(ErrorCode.TOSS_PAYMENT_CREATE_FAILED,
                        tossResponse.getMsg() != null ? tossResponse.getMsg() : "결제 생성에 실패했습니다.");
            }

            payment.updateCheckoutInfo(tossResponse.getCheckoutPage(), tossResponse.getPayToken());
            payment = paymentRepository.save(payment);

            return paymentMapper.toResponse(payment);
        } catch (TossPaymentClientException e) {
            log.error("토스페이먼츠 API 호출 중 오류 발생", e);
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, e.getMessage());
        }
    }

    @Override
    public PaymentApprovalResponse approvePayment(ApprovePaymentCommand command) {
        log.info("결제 승인 요청: payToken={}, orderNo={}", command.getPayToken(), command.getOrderNo());

        Payment payment = findPayment(command);
        validatePaymentStatusForApproval(payment);
        String payToken = determinePayToken(command, payment);
        TossPaymentExecuteResponse executeResponse = executePaymentWithToss(payToken, command.getOrderNo());
        updatePaymentWithApproval(payment, executeResponse);
        updatePaymentMethodInfo(payment, executeResponse);
        payment = paymentRepository.save(payment);

        log.info("결제 승인 완료: orderNo={}, payToken={}, transactionId={}",
                payment.getOrderNo(), payment.getPayToken(), payment.getTransactionId());

        return paymentMapper.toApprovalResponse(payment);
    }

    private Payment findPayment(ApprovePaymentCommand command) {
        if (command.getPayToken() != null && !command.getPayToken().isBlank()) {
            return paymentRepository.findByPayToken(command.getPayToken())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                            String.format("결제 토큰으로 결제 정보를 찾을 수 없습니다. payToken: %s", command.getPayToken())));
        }

        if (command.getOrderNo() != null && !command.getOrderNo().isBlank()) {
            return paymentRepository.findByOrderNo(command.getOrderNo())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                            String.format("주문번호로 결제 정보를 찾을 수 없습니다. orderNo: %s", command.getOrderNo())));
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                "결제 토큰 또는 주문번호 중 하나는 필수입니다.");
    }

    private void validatePaymentStatusForApproval(Payment payment) {
        if (PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            log.warn("이미 완료된 결제입니다. orderNo: {}, status: {}", payment.getOrderNo(), payment.getStatus());
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_COMPLETED,
                    String.format("이미 완료된 결제입니다. orderNo: %s", payment.getOrderNo()));
        }

        if (!PaymentStatus.PENDING.equals(payment.getStatus()) && 
            !PaymentStatus.APPROVED.equals(payment.getStatus())) {
            log.warn("결제 승인 대기 상태가 아닙니다. orderNo: {}, status: {}", 
                    payment.getOrderNo(), payment.getStatus());
            throw new BusinessException(ErrorCode.PAYMENT_NOT_APPROVED,
                    String.format("결제 승인 대기 상태가 아닙니다. 현재 상태: %s, orderNo: %s", 
                            payment.getStatus(), payment.getOrderNo()));
        }
    }

    private String determinePayToken(ApprovePaymentCommand command, Payment payment) {
        if (command.getPayToken() != null && !command.getPayToken().isBlank()) {
            if (payment.getPayToken() != null && !payment.getPayToken().equals(command.getPayToken())) {
                log.error("결제 토큰이 일치하지 않습니다. orderNo: {}, expected: {}, actual: {}",
                        payment.getOrderNo(), payment.getPayToken(), command.getPayToken());
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        String.format("결제 토큰이 일치하지 않습니다. orderNo: %s", payment.getOrderNo()));
            }
            return command.getPayToken();
        }

        if (payment.getPayToken() == null || payment.getPayToken().isBlank()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                    String.format("결제 토큰이 없습니다. orderNo: %s", payment.getOrderNo()));
        }

        return payment.getPayToken();
    }

    private TossPaymentExecuteResponse executePaymentWithToss(String payToken, String orderNo) {
        try {
            TossPaymentExecuteRequest executeRequest = TossPaymentExecuteRequest.builder()
                    .payToken(payToken)
                    .orderNo(orderNo)
                    .build();
            
            TossPaymentExecuteResponse executeResponse = tossPaymentClient.executePayment(executeRequest);

            if (!executeResponse.isSuccess()) {
                log.error("토스페이먼츠 결제 승인 실패: code={}, msg={}, errorCode={}",
                        executeResponse.getCode(), executeResponse.getMsg(), executeResponse.getErrorCode());
                throw new BusinessException(ErrorCode.TOSS_PAYMENT_EXECUTE_FAILED,
                        executeResponse.getMsg() != null ? executeResponse.getMsg() : "결제 승인에 실패했습니다.");
            }

            return executeResponse;
        } catch (TossPaymentClientException e) {
            log.error("토스페이먼츠 결제 승인 API 호출 중 오류 발생", e);
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, e.getMessage());
        }
    }

    private void updatePaymentWithApproval(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.approvePayment(
                executeResponse.getMode(),
                executeResponse.getApprovalTime(),
                executeResponse.getStateMsg(),
                executeResponse.getPayMethod(),
                executeResponse.getDiscountedAmount(),
                executeResponse.getPaidAmount(),
                executeResponse.getTransactionId(),
                executeResponse.getCashReceiptMgtKey()
        );
    }

    private void updatePaymentMethodInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        String payMethod = executeResponse.getPayMethod();
        if (payMethod == null) {
            return;
        }

        if ("CARD".equals(payMethod)) {
            updateCardInfo(payment, executeResponse);
        } else if ("TOSS_MONEY".equals(payMethod)) {
            updateAccountInfo(payment, executeResponse);
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
                executeResponse.getCardNum4Print()
        );
    }

    private void updateAccountInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.updateApprovalAccountInfo(
                executeResponse.getAccountBankCode(),
                executeResponse.getAccountBankName(),
                executeResponse.getAccountNumber()
        );
    }

    public PaymentStatusResponse getPaymentStatus(GetPaymentStatusCommand command) {
        log.info("결제 상태 확인 요청: payToken={}, orderNo={}", command.getPayToken(), command.getOrderNo());

        try {
            TossPaymentStatusRequest statusRequest = TossPaymentStatusRequest.builder()
                    .payToken(command.getPayToken())
                    .orderNo(command.getOrderNo())
                    .build();

            TossPaymentStatusResponse statusResponse = tossPaymentClient.getPaymentStatus(statusRequest);

            if (!statusResponse.isSuccess()) {
                log.error("토스페이먼츠 결제 상태 확인 실패: code={}, msg={}, errorCode={}",
                        statusResponse.getCode(), statusResponse.getMsg(), statusResponse.getErrorCode());
                throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR,
                        statusResponse.getMsg() != null ? statusResponse.getMsg() : "결제 상태 확인에 실패했습니다.");
            }

            return paymentMapper.toStatusResponse(statusResponse);
        } catch (TossPaymentClientException e) {
            log.error("토스페이먼츠 결제 상태 확인 API 호출 중 오류 발생", e);
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, e.getMessage());
        }
    }
}

