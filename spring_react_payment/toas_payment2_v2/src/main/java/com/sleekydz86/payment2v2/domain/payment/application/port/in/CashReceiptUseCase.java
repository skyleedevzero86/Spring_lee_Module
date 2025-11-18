package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.*;

import java.util.List;

public interface CashReceiptUseCase {
    CashReceiptResponse issueCashReceipt(IssueCashReceiptCommand command);
    CashReceiptResponse cancelCashReceipt(CancelCashReceiptCommand command);
    CashReceiptListResponse getCashReceipts(String requestDate, Long cursor, Integer limit);
}
