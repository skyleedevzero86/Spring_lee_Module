package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.*;
import com.sleekydz86.payment2v2.domain.payment.application.dto.*;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.CashReceiptUseCase;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.global.constants.HeaderConstants;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cash-receipts")
@RequiredArgsConstructor
public class CashReceiptController {

    private final CashReceiptUseCase cashReceiptUseCase;
    private final CashReceiptWebMapper cashReceiptWebMapper;

    @PostMapping
    public ResponseEntity<CashReceiptApiResponse> issueCashReceipt(
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody IssueCashReceiptRequest request) {
        MemberId memberId = MemberId.of(userId);
        return LoggingUtil.executeWithContext("userId", String.valueOf(memberId.getValue()), () -> {
            log.info("현금영수증 발급 요청: userId={}, orderId={}", userId, request.getOrderId());

            IssueCashReceiptCommand command = cashReceiptWebMapper.toIssueCommand(request);
            CashReceiptResponse response = cashReceiptUseCase.issueCashReceipt(command);
            CashReceiptApiResponse apiResponse = cashReceiptWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        });
    }

    @PostMapping("/{receiptKey}/cancel")
    public ResponseEntity<CashReceiptApiResponse> cancelCashReceipt(
            @PathVariable String receiptKey,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody CancelCashReceiptRequest request) {
        MemberId memberId = MemberId.of(userId);
        return LoggingUtil.executeWithContext("userId", String.valueOf(memberId.getValue()), () -> {
            log.info("현금영수증 취소 요청: userId={}, receiptKey={}", userId, receiptKey);

            CancelCashReceiptCommand command = cashReceiptWebMapper.toCancelCommand(receiptKey, request);
            CashReceiptResponse response = cashReceiptUseCase.cancelCashReceipt(command);
            CashReceiptApiResponse apiResponse = cashReceiptWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        });
    }

    @GetMapping
    public ResponseEntity<CashReceiptListApiResponse> getCashReceipts(
            @RequestParam String requestDate,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestHeader(HeaderConstants.USER_ID_HEADER) Long userId) {
        MemberId memberId = MemberId.of(userId);
        return LoggingUtil.executeWithContext("userId", String.valueOf(memberId.getValue()), () -> {
            log.info("현금영수증 조회 요청: userId={}, requestDate={}, cursor={}, limit={}",
                    userId, requestDate, cursor, limit);

            CashReceiptListResponse response = cashReceiptUseCase.getCashReceipts(requestDate, cursor, limit);
            CashReceiptListApiResponse apiResponse = cashReceiptWebMapper.toListApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        });
    }
}

