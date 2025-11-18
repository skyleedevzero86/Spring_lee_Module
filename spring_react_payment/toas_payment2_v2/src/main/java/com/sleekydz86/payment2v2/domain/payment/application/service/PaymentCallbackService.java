package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.port.in.ProcessPaymentCallbackUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.InventoryPort;
import com.sleekydz86.payment2v2.global.constants.PaymentConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentCallbackCommand;
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
public class PaymentCallbackService implements ProcessPaymentCallbackUseCase {

    private final PaymentRepository paymentRepository;
    private final InventoryPort inventoryPort;

    @Override
    @Transactional
    public void processCallback(PaymentCallbackCommand callbackCommand) {
        log.info("결제 콜백 처리 시작: orderNo={}, status={}, payToken={}",
                callbackCommand.getOrderNo(), callbackCommand.getStatus(), callbackCommand.getPayToken());

        Payment payment = findPaymentByOrderNo(callbackCommand.getOrderNo());
        validatePaymentStatus(payment, callbackCommand);
        validatePayToken(payment, callbackCommand.getPayToken());
        validatePaymentAmount(payment, callbackCommand.getAmount());

        if (!PaymentConstants.PAY_COMPLETE_STATUS.equals(callbackCommand.getStatus())) {
            log.warn("결제 완료 상태가 아닙니다. status: {}, orderNo: {}",
                    callbackCommand.getStatus(), callbackCommand.getOrderNo());
            throw new BusinessException(ErrorCode.CALLBACK_INVALID_STATUS,
                    String.format("유효하지 않은 결제 상태입니다. status: %s", callbackCommand.getStatus()));
        }

        updatePaymentInfo(payment, callbackCommand);
        updatePaymentMethodInfo(payment, callbackCommand);
        paymentRepository.save(payment);

        processPostPaymentActions(payment);

        log.info("결제 완료 처리 완료. orderNo: {}, payToken: {}, transactionId: {}",
                callbackCommand.getOrderNo(), callbackCommand.getPayToken(), callbackCommand.getTransactionId());
    }

    private Payment findPaymentByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            log.error("주문번호가 null이거나 비어있습니다.");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "주문번호는 필수입니다.");
        }

        return paymentRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> {
                    log.warn("주문번호로 결제 정보를 찾을 수 없음: orderNo={}", orderNo);
                    return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                            String.format("주문번호로 결제 정보를 찾을 수 없습니다. orderNo: %s", orderNo));
                });
    }

    private void validatePaymentStatus(Payment payment, PaymentCallbackCommand callbackCommand) {
        if (PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            log.warn("이미 완료된 결제입니다. orderNo: {}", callbackCommand.getOrderNo());
            throw new BusinessException(ErrorCode.CALLBACK_PAYMENT_ALREADY_COMPLETED,
                    String.format("이미 완료된 결제입니다. orderNo: %s", callbackCommand.getOrderNo()));
        }
    }

    private void validatePayToken(Payment payment, String payToken) {
        if (payToken == null || payToken.isBlank()) {
            log.warn("결제 토큰이 null이거나 비어있습니다. orderNo={}", payment.getOrderNoValue());
            return;
        }

        if (payment.getPayToken() != null && !payment.getPayToken().equals(payToken)) {
            log.error("결제 토큰이 일치하지 않습니다. orderNo: {}, expected: {}, actual: {}",
                    payment.getOrderNoValue(), payment.getPayToken(), payToken);
            throw new BusinessException(ErrorCode.CALLBACK_INVALID_PAY_TOKEN,
                    String.format("결제 토큰이 일치하지 않습니다. orderNo: %s", payment.getOrderNoValue()));
        }
    }

    private void validatePaymentAmount(Payment payment, Integer callbackAmount) {
        if (callbackAmount == null) {
            log.debug("콜백 금액이 null입니다. 검증을 건너뜁니다. orderNo={}", payment.getOrderNoValue());
            return;
        }

        BigDecimal expectedAmount = payment.getAmount();
        if (expectedAmount == null) {
            log.error("결제 금액이 null입니다. 데이터 무결성 문제 가능성. orderNo={}", payment.getOrderNoValue());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    String.format("결제 금액이 null입니다. orderNo: %s", payment.getOrderNoValue()));
        }

        BigDecimal actualAmount = BigDecimal.valueOf(callbackAmount);

        if (expectedAmount.compareTo(actualAmount) != 0) {
            log.error("결제 금액이 일치하지 않습니다. orderNo: {}, expected: {}, actual: {}",
                    payment.getOrderNoValue(), expectedAmount, actualAmount);
            throw new BusinessException(ErrorCode.CALLBACK_INVALID_AMOUNT,
                    String.format("결제 금액이 일치하지 않습니다. orderNo: %s, expected: %s, actual: %s",
                            payment.getOrderNoValue(), expectedAmount, actualAmount));
        }
    }

    private void updatePaymentInfo(Payment payment, PaymentCallbackCommand callbackCommand) {
        payment.completePayment(
                callbackCommand.getPayMethod(),
                callbackCommand.getDiscountedAmount() != null ? BigDecimal.valueOf(callbackCommand.getDiscountedAmount()) : null,
                callbackCommand.getPaidAmount() != null ? BigDecimal.valueOf(callbackCommand.getPaidAmount()) : null,
                callbackCommand.getPaidTs(),
                callbackCommand.getTransactionId()
        );
    }

    private void updatePaymentMethodInfo(Payment payment, PaymentCallbackCommand callbackCommand) {
        String payMethod = callbackCommand.getPayMethod();
        if (payMethod == null) {
            log.debug("결제 수단이 null입니다.");
            return;
        }

        if (PaymentConstants.PAY_METHOD_CARD.equals(payMethod)) {
            updateCardInfo(payment, callbackCommand);
        } else if (PaymentConstants.PAY_METHOD_TOSS_MONEY.equals(payMethod)) {
            updateAccountInfo(payment, callbackCommand);
        }
    }

    private void updateCardInfo(Payment payment, PaymentCallbackCommand callbackCommand) {
        Integer spreadOut = parseSpreadOut(callbackCommand.getSpreadOut(), payment.getOrderNoValue());

        payment.updateCardInfo(
                callbackCommand.getCardCompanyCode(),
                callbackCommand.getCardAuthorizationNo(),
                spreadOut,
                callbackCommand.getNoInterest(),
                callbackCommand.getCardMethodType(),
                callbackCommand.getCardUserType(),
                callbackCommand.getCardBinNumber(),
                callbackCommand.getCardNum4Print(),
                callbackCommand.getSalesCheckLinkUrl()
        );
    }

    private Integer parseSpreadOut(String spreadOutValue, String orderNo) {
        if (spreadOutValue == null || spreadOutValue.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(spreadOutValue);
        } catch (NumberFormatException e) {
            log.error("spreadOut 파싱 실패: spreadOut={}, orderNo={}", spreadOutValue, orderNo, e);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("spreadOut 파싱에 실패했습니다. spreadOut: %s, orderNo: %s", spreadOutValue, orderNo));
        }
    }

    private void updateAccountInfo(Payment payment, PaymentCallbackCommand callbackCommand) {
        payment.updateAccountInfo(
                callbackCommand.getAccountBankCode(),
                callbackCommand.getAccountBankName(),
                callbackCommand.getAccountNumber()
        );
    }

    private void processPostPaymentActions(Payment payment) {
        try {
            inventoryPort.deductInventory(payment.getOrderNoValue());
            log.info("재고 차감 완료. orderNo: {}", payment.getOrderNoValue());
        } catch (Exception e) {
            log.error("재고 차감 중 오류 발생. orderNo: {}", payment.getOrderNoValue(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    String.format("재고 차감 중 오류가 발생했습니다. orderNo: %s", payment.getOrderNoValue()), e);
        }
    }
}
