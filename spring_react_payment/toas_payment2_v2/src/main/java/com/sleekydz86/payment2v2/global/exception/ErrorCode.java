package com.sleekydz86.payment2v2.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C000", "인증이 필요합니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    DUPLICATE_ORDER_NO(HttpStatus.BAD_REQUEST, "P001", "이미 사용된 주문번호입니다."),
    TOSS_PAYMENT_CREATE_FAILED(HttpStatus.BAD_REQUEST, "P002", "토스페이먼츠 결제 생성에 실패했습니다."),
    TOSS_PAYMENT_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "P003", "토스페이먼츠 API 호출 중 오류가 발생했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P004", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "P005", "이미 완료된 결제입니다."),
    PAYMENT_NOT_APPROVED(HttpStatus.BAD_REQUEST, "P006", "결제 승인 대기 상태가 아닙니다."),
    TOSS_PAYMENT_EXECUTE_FAILED(HttpStatus.BAD_REQUEST, "P007", "토스페이먼츠 결제 승인에 실패했습니다."),
    CALLBACK_INVALID_STATUS(HttpStatus.BAD_REQUEST, "P008", "유효하지 않은 결제 상태입니다."),
    CALLBACK_PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "P009", "이미 완료된 결제입니다."),
    CALLBACK_INVALID_PAY_TOKEN(HttpStatus.BAD_REQUEST, "P010", "결제 토큰이 일치하지 않습니다."),
    CALLBACK_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "P011", "결제 금액이 일치하지 않습니다."),
    CALLBACK_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P012", "결제 콜백 처리 중 오류가 발생했습니다."),
    PAYMENT_APPROVAL_INVALID_STATUS(HttpStatus.BAD_REQUEST, "P013", "결제 승인 가능한 상태가 아닙니다."),
    PAYMENT_TOKEN_MISMATCH(HttpStatus.BAD_REQUEST, "P014", "결제 토큰이 일치하지 않습니다."),
    INVALID_DATA_FORMAT(HttpStatus.BAD_REQUEST, "P015", "데이터 형식이 올바르지 않습니다."),
    AMOUNT_EXCEEDS_INTEGER_RANGE(HttpStatus.BAD_REQUEST, "P016", "금액이 범위를 초과합니다."),
    PAYMENT_REFUND_FAILED(HttpStatus.BAD_REQUEST, "P017", "결제 환불에 실패했습니다."),
    PAYMENT_NOT_REFUNDABLE(HttpStatus.BAD_REQUEST, "P018", "환불 가능한 상태가 아닙니다."),
    REFUND_AMOUNT_EXCEEDS_REFUNDABLE(HttpStatus.BAD_REQUEST, "P019", "환불 요청 금액이 환불 가능 금액을 초과합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원 정보를 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "M002", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "M003", "비밀번호가 일치하지 않습니다."),
    MEMBER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "M004", "해당 이메일로 등록된 회원을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

