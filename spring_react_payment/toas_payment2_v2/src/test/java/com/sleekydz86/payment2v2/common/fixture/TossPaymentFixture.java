package com.sleekydz86.payment2v2.common.fixture;

import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.*;

public class TossPaymentFixture {

    public static TossPaymentResponse 성공한_결제_생성_응답() {
        TossPaymentResponse response = new TossPaymentResponse();
        response.setCode(0);
        response.setMsg("정상 처리되었습니다.");
        response.setCheckoutPage("https://toss.im/checkout/test-token");
        response.setPayToken("test-pay-token-123");
        return response;
    }

    public static TossPaymentResponse 실패한_결제_생성_응답() {
        TossPaymentResponse response = new TossPaymentResponse();
        response.setCode(400);
        response.setMsg("결제 생성에 실패했습니다.");
        response.setErrorCode("INVALID_REQUEST");
        return response;
    }

    public static TossPaymentExecuteResponse 성공한_결제_승인_응답() {
        TossPaymentExecuteResponse response = new TossPaymentExecuteResponse();
        response.setCode(0);
        response.setMsg("정상 승인되었습니다.");
        response.setMode("NORMAL");
        response.setApprovalTime("20240101120000");
        response.setStateMsg("정상승인");
        response.setPayMethod("카드");
        response.setDiscountedAmount(0);
        response.setPaidAmount(10000);
        response.setTransactionId("TXN-123456");
        response.setCardCompanyName("신한카드");
        response.setCardCompanyCode(1);
        return response;
    }

    public static TossPaymentRefundResponse 성공한_환불_응답() {
        TossPaymentRefundResponse response = new TossPaymentRefundResponse();
        response.setCode(0);
        response.setMsg("환불이 완료되었습니다.");
        response.setRefundableAmount(10000);
        response.setRefundedAmount(10000);
        response.setApprovalTime("20240101130000");
        response.setTransactionId("TXN-REFUND-123");
        return response;
    }

    public static TossPaymentStatusResponse 결제_상태_응답(String status) {
        TossPaymentStatusResponse response = new TossPaymentStatusResponse();
        response.setCode(0);
        response.setMsg("정상 처리되었습니다.");
        response.setPayStatus(status);
        response.setOrderNo("ORDER-001");
        response.setAmount(10000);
        return response;
    }

