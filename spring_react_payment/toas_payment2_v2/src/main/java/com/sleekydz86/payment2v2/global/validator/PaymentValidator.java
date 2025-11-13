package com.sleekydz86.payment2v2.global.validator;

import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.Money;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.OrderNo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentValidator {

    public MemberId validateUserId(Long userId) {
        return MemberId.of(userId);
    }

    public OrderNo validateOrderNo(String orderNo) {
        return OrderNo.of(orderNo);
    }

    public Money validateAmount(BigDecimal amount) {
        return Money.of(amount);
    }

    public Money validateAmountTaxFree(BigDecimal amountTaxFree) {
        return Money.of(amountTaxFree);
    }
}

