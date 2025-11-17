package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentCallbackCommand;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCallbackRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentCallbackWebMapper {

    public PaymentCallbackCommand toCommand(TossPaymentCallbackRequest request) {
        return PaymentCallbackCommand.builder()
                .status(request.getStatus())
                .payToken(request.getPayToken())
                .orderNo(request.getOrderNo())
                .payMethod(request.getPayMethod())
                .amount(request.getAmount())
                .discountedAmount(request.getDiscountedAmount())
                .paidAmount(request.getPaidAmount())
                .paidTs(request.getPaidTs())
                .transactionId(request.getTransactionId())
                .cardCompanyCode(request.getCardCompanyCode())
                .cardAuthorizationNo(request.getCardAuthorizationNo())
                .spreadOut(request.getSpreadOut())
                .noInterest(request.getNoInterest())
                .cardMethodType(request.getCardMethodType())
                .cardUserType(request.getCardUserType())
                .cardNumber(request.getCardNumber())
                .cardBinNumber(request.getCardBinNumber())
                .cardNum4Print(request.getCardNum4Print())
                .salesCheckLinkUrl(request.getSalesCheckLinkUrl())
                .accountBankCode(request.getAccountBankCode())
                .accountBankName(request.getAccountBankName())
                .accountNumber(request.getAccountNumber())
                .build();
    }
}


