package com.sleekydz86.ftpserver.domain.ftp;

import lombok.Getter;

@Getter
public enum FtpTransferStatus {
    PENDING("대기중"),
    IN_PROGRESS("진행중"),
    SUCCESS("성공"),
    FAILED("실패"),
    RETRYING("재시도중"),
    CANCELLED("취소됨");

    private final String description;

    FtpTransferStatus(String description) {
        this.description = description;
    }

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }

    public boolean canRetry() {
        return this == FAILED || this == RETRYING;
    }
}
