package com.example.tts.global.exception;

import com.example.tts.speech.domain.exception.SpeechRuleException;
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

    @ExceptionHandler(SpeechRuleException.class)
    public ResponseEntity<ErrorResponse> handleRule(SpeechRuleException exception) {
        log.warn("음성 요청 규칙에 맞지 않습니다. code={}, message={}", exception.code(), exception.getMessage());
        HttpStatus status = "NOT_FOUND".equals(exception.code()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ErrorResponse(exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternal(ExternalServiceException exception) {
        log.error("외부 음성 서비스 처리에 실패했습니다. code={}, message={}", exception.code(), exception.getMessage());
        HttpStatus status = "CREDENTIALS_MISSING".equals(exception.code())
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.BAD_GATEWAY;
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
}
