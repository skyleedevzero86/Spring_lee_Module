package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.port.in.ProcessPaymentCallbackUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.InventoryPort;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCallbackRequest;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.PaymentStatus;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCallbackService implements ProcessPaymentCallbackUseCase {

    private static final String PAY_COMPLETE_STATUS = "PAY_COMPLETE";
    private static final String PAY_METHOD_CARD = "CARD";
    private static final String PAY_METHOD_TOSS_MONEY = "TOSS_MONEY";

    private final PaymentRepository paymentRepository;
    private final InventoryPort inventoryPort;

    @Override
    public void processCallback(TossPaymentCallbackRequest callbackRequest) {
        log.info("결제 콜백 처리 시작: orderNo={}, status={}, payToken={}",
                callbackRequest.getOrderNo(), callbackRequest.getStatus(), callbackRequest.getPayToken());

        Payment payment = findPaymentByOrderNo(callbackRequest.getOrderNo());
        validatePaymentStatus(payment, callbackRequest);
        validatePayToken(payment, callbackRequest.getPayToken());
        validatePaymentAmount(payment, callbackRequest.getAmount());

        if (!PAY_COMPLETE_STATUS.equals(callbackRequest.getStatus())) {
            log.warn("결제 완료 상태가 아닙니다. status: {}, orderNo: {}",
                    callbackRequest.getStatus(), callbackRequest.getOrderNo());
            throw new BusinessException(ErrorCode.CALLBACK_INVALID_STATUS,
                    String.format("유효하지 않은 결제 상태입니다. status: %s", callbackRequest.getStatus()));
        }

        updatePaymentInfo(payment, callbackRequest);
        updatePaymentMethodInfo(payment, callbackRequest);
        paymentRepository.save(payment);
        processPostPaymentActions(payment);

        log.info("결제 완료 처리 완료. orderNo: {}, payToken: {}, transactionId: {}",
                callbackRequest.getOrderNo(), callbackRequest.getPayToken(), callbackRequest.getTransactionId());
    }

    private Payment findPaymentByOrderNo(String orderNo) {
        return paymentRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                        String.format("주문번호로 결제 정보를 찾을 수 없습니다. orderNo: %s", orderNo)));
    }

    private void validatePaymentStatus(Payment payment, TossPaymentCallbackRequest callbackRequest) {
        if (PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            log.warn("이미 완료된 결제입니다. orderNo: {}", callbackRequest.getOrderNo());
            throw new BusinessException(ErrorCode.CALLBACK_PAYMENT_ALREADY_COMPLETED,
                    String.format("이미 완료된 결제입니다. orderNo: %s", callbackRequest.getOrderNo()));
        }
    }

    private void validatePayToken(Payment payment, String payToken) {
        if (payment.getPayToken() != null && !payment.getPayToken().equals(payToken)) {
            log.error("결제 토큰이 일치하지 않습니다. orderNo: {}, expected: {}, actual: {}",
                    payment.getOrderNo(), payment.getPayToken(), payToken);
            throw new BusinessException(ErrorCode.CALLBACK_INVALID_PAY_TOKEN,
                    String.format("결제 토큰이 일치하지 않습니다. orderNo: %s", payment.getOrderNo()));
        }
    }

    private void validatePaymentAmount(Payment payment, Integer callbackAmount) {
        if (callbackAmount == null) {
            return;
        }

        BigDecimal expectedAmount = payment.getAmount();
        BigDecimal actualAmount = BigDecimal.valueOf(callbackAmount);

        if (expectedAmount.compareTo(actualAmount) != 0) {
            log.error("결제 금액이 일치하지 않습니다. orderNo: {}, expected: {}, actual: {}",
                    payment.getOrderNo(), expectedAmount, actualAmount);
            throw new BusinessException(ErrorCode.CALLBACK_INVALID_AMOUNT,
                    String.format("결제 금액이 일치하지 않습니다. orderNo: %s, expected: %s, actual: %s",
                            payment.getOrderNo(), expectedAmount, actualAmount));
        }
    }

    private void updatePaymentInfo(Payment payment, TossPaymentCallbackRequest callbackRequest) {
        payment.completePayment(
                callbackRequest.getPayMethod(),
                callbackRequest.getDiscountedAmount(),
                callbackRequest.getPaidAmount(),
                callbackRequest.getPaidTs(),
                callbackRequest.getTransactionId()
        );
    }

    private void updatePaymentMethodInfo(Payment payment, TossPaymentCallbackRequest callbackRequest) {
        String payMethod = callbackRequest.getPayMethod();

        if (PAY_METHOD_CARD.equals(payMethod)) {
            updateCardInfo(payment, callbackRequest);
        } else if (PAY_METHOD_TOSS_MONEY.equals(payMethod)) {
            updateAccountInfo(payment, callbackRequest);
        }
    }

    private void updateCardInfo(Payment payment, TossPaymentCallbackRequest callbackRequest) {
        payment.updateCardInfo(
                callbackRequest.getCardCompanyCode(),
                callbackRequest.getCardAuthorizationNo(),
                callbackRequest.getSpreadOut(),
                callbackRequest.getNoInterest(),
                callbackRequest.getCardMethodType(),
                callbackRequest.getCardUserType(),
                callbackRequest.getCardBinNumber(),
                callbackRequest.getCardNum4Print(),
                callbackRequest.getSalesCheckLinkUrl()
        );
    }

    private void updateAccountInfo(Payment payment, TossPaymentCallbackRequest callbackRequest) {
        payment.updateAccountInfo(
                callbackRequest.getAccountBankCode(),
                callbackRequest.getAccountBankName(),
                callbackRequest.getAccountNumber()
        );
    }

    private void processPostPaymentActions(Payment payment) {
        try {
            inventoryPort.deductInventory(payment.getOrderNo());
            log.info("재고 차감 완료. orderNo: {}", payment.getOrderNo());
        } catch (Exception e) {
            log.error("재고 차감 중 오류 발생. orderNo: {}", payment.getOrderNo(), e);
        }
    }
}

