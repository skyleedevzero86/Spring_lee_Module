package com.sleekydz86.storagemigration.global.error;

import com.sleekydz86.storagemigration.global.response.ApiResponse;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        if (errorCode.status().is5xxServerError()) {
            log.warn("[{}] {}", errorCode.code(), exception.getMessage(), exception.getCause());
        } else {
            log.info("[{}] {}", errorCode.code(), exception.getMessage());
        }
        return respond(errorCode, exception.detail());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return respond(ErrorCode.INVALID_INPUT, detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException exception) {
        return respond(ErrorCode.INVALID_INPUT, "요청 본문(JSON)을 읽을 수 없습니다. 형식과 값을 확인하세요.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException exception) {
        return respond(ErrorCode.INVALID_INPUT, "필수 파라미터가 없습니다: " + exception.getParameterName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPart(MissingServletRequestPartException exception) {
        return respond(ErrorCode.INVALID_INPUT, "업로드할 파일이 없습니다: " + exception.getRequestPartName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return respond(ErrorCode.INVALID_INPUT, "파라미터 형식이 올바르지 않습니다: " + exception.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethod(HttpRequestMethodNotSupportedException exception) {
        return respond(ErrorCode.INVALID_INPUT, "지원하지 않는 요청 방식입니다: " + exception.getMethod());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException exception) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(new ErrorResponse("C404", "요청한 주소를 찾을 수 없습니다.",
                        exception.getResourcePath())));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleUploadSize(MaxUploadSizeExceededException exception) {
        return respond(ErrorCode.FILE_TOO_LARGE, "업로드 크기 제한(500MB)을 넘었습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("예상하지 못한 오류가 발생했습니다.", exception);
        return respond(ErrorCode.INTERNAL_ERROR, null);
    }

    private static ResponseEntity<ApiResponse<Void>> respond(ErrorCode errorCode, String detail) {
        return ResponseEntity.status(errorCode.status()).body(ApiResponse.fail(ErrorResponse.of(errorCode, detail)));
    }
}
