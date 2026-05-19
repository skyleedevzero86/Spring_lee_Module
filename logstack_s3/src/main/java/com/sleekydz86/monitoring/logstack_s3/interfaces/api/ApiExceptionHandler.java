package com.sleekydz86.monitoring.logstack_s3.interfaces.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.sleekydz86.monitoring.logstack_s3.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileStorageException;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.InvalidRequestException;
import com.sleekydz86.monitoring.logstack_s3.interfaces.api.dto.ApiErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFileNotFound(FileNotFoundException ex) {
        log.warn("파일 조회 실패: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        log.warn("잘못된 요청: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("요청 값 오류: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({FileStorageException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleStorage(RuntimeException ex) {
        log.error("파일 저장/처리 오류: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        log.warn("업로드 용량 초과: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiErrorResponse(KoreanMessages.FILE_TOO_LARGE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        log.error("알 수 없는 서버 오류", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(KoreanMessages.INTERNAL_ERROR));
    }
}
