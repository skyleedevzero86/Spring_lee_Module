package com.sleekydevz86.tts.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(TtsException.class)
    public ResponseEntity<ErrorResponse> handle(TtsException exception) {
        HttpStatus status = status(exception.code());
        if (status.is5xxServerError()) {
            log.error("음성 처리에 실패했습니다. code={}, message={}", exception.code(), exception.getMessage());
        } else {
            log.warn("음성 요청을 처리할 수 없습니다. code={}, message={}", exception.code(), exception.getMessage());
        }
        return ResponseEntity.status(status).body(new ErrorResponse(exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
        log.warn("요청 본문을 읽지 못했습니다.", exception);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_TEXT", "요청 본문이 올바른 JSON이 아닙니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("처리하지 못한 오류가 발생했습니다.", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("TTS_FAILED", "음성 처리 중 오류가 발생했습니다."));
    }

    private HttpStatus status(String code) {
        return switch (code) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CREDENTIALS_MISSING" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "TTS_FAILED" -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
