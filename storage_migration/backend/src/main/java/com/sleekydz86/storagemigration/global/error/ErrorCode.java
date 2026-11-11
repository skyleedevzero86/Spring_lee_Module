package com.sleekydz86.storagemigration.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    DB_CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "DB 연결 정보를 찾을 수 없습니다."),
    DB_NOT_CONNECTED(HttpStatus.CONFLICT, "D002", "연결된 DB가 없습니다. DB 연결을 먼저 활성화하세요."),
    DB_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "D003", "DB에 연결하지 못했습니다."),
    DB_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "D004", "DB 작업에 실패했습니다."),
    DB_CONNECTION_NOT_TESTED(HttpStatus.CONFLICT, "D005", "연결 테스트에 성공한 DB만 활성화할 수 있습니다."),
    STORAGE_SERVER_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "스토리지 서버 정보를 찾을 수 없습니다."),
    STORAGE_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "S002", "스토리지 서버에 연결하지 못했습니다."),
    STORAGE_OPERATION_FAILED(HttpStatus.BAD_GATEWAY, "S003", "스토리지 작업에 실패했습니다."),
    STORAGE_OBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "S004", "스토리지에서 파일을 찾을 수 없습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "파일 정보를 찾을 수 없습니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "F002", "파일이 DB에 저장할 수 있는 크기를 넘습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "F003", "빈 파일은 업로드할 수 없습니다."),
    FILE_NOT_SAVED_IN_DB(HttpStatus.CONFLICT, "F004", "DB에 저장된 파일 내용이 없습니다. 먼저 DB에 저장하세요."),
    CRYPTO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "암호화 처리에 실패했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G999", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
