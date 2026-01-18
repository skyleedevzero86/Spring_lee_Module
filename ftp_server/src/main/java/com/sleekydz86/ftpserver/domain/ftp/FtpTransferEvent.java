package com.sleekydz86.ftpserver.domain.ftp;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class FtpTransferEvent {
    private final FtpTransferId transferId;
    private final String eventType;
    private final String message;
    private final LocalDateTime occurredAt;

    public FtpTransferEvent(FtpTransferId transferId, String eventType, String message) {
        this.transferId = transferId;
        this.eventType = eventType;
        this.message = message;
        this.occurredAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", occurredAt, eventType, message);
    }
}
