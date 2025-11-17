package com.sleekydz86.payment2v2.domain.payment.application.port.out;

import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.*;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossCashReceiptCancelRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossCashReceiptListResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossCashReceiptRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossCashReceiptResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCancelRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCancelResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusResponse;

public interface PaymentGatewayPort {
    TossPaymentResponse createPayment(TossPaymentRequest request);
    TossPaymentExecuteResponse executePayment(TossPaymentExecuteRequest request);
    TossPaymentStatusResponse getPaymentStatus(TossPaymentStatusRequest request);
    TossPaymentRefundResponse refundPayment(TossPaymentRefundRequest request);
    TossPaymentCancelResponse cancelPayment(String paymentKey, TossPaymentCancelRequest request);
    TossCashReceiptResponse issueCashReceipt(TossCashReceiptRequest request);
    TossCashReceiptResponse cancelCashReceipt(String receiptKey, TossCashReceiptCancelRequest request);
    TossCashReceiptListResponse getCashReceipts(String requestDate, Long cursor, Integer limit);
}



