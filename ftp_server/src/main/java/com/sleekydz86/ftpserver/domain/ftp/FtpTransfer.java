package com.sleekydz86.ftpserver.domain.ftp;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class FtpTransfer {
    private FtpTransferId transferId;
    private String paymentRequestNo;
    private String remotePath;
    private String fileName;
    private byte[] fileContent;
    private FtpTransferStatus transferStatus;
    private String errorMessage;
    private int retryCount;
    private int maxRetries;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<FtpTransferEvent> eventList;

    private FtpTransfer() {
        this.eventList = new ArrayList<>();
    }

    public static FtpTransfer create(String paymentRequestNo, String remotePath, String fileName, byte[] fileContent) {
        FtpTransfer transfer = new FtpTransfer();
        transfer.transferId = FtpTransferId.generate();
        transfer.paymentRequestNo = paymentRequestNo;
        transfer.remotePath = remotePath;
        transfer.fileName = fileName;
        transfer.fileContent = fileContent;
        transfer.transferStatus = FtpTransferStatus.PENDING;
        transfer.maxRetries = 3;
        transfer.retryCount = 0;
        transfer.createdAt = LocalDateTime.now();
        transfer.addEvent(new FtpTransferEvent(transfer.transferId, "FTP_TRANSFER_CREATED", "FTP 전송 요청이 생성되었습니다: " + paymentRequestNo));
        return transfer;
    }

    public void start() {
        Predicate<FtpTransferStatus> canStart = status -> status == FtpTransferStatus.PENDING || status == FtpTransferStatus.RETRYING;
        if (!canStart.test(this.transferStatus)) {
            throw new IllegalStateException("FTP 전송을 시작할 수 없는 상태입니다: " + this.transferStatus);
        }
        this.transferStatus = FtpTransferStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.addEvent(new FtpTransferEvent(this.transferId, "FTP_TRANSFER_STARTED", "FTP 전송이 시작되었습니다"));
    }

    public void complete() {
        if (this.transferStatus != FtpTransferStatus.IN_PROGRESS) {
            throw new IllegalStateException("FTP 전송을 완료할 수 없는 상태입니다: " + this.transferStatus);
        }
        this.transferStatus = FtpTransferStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = null;
        this.addEvent(new FtpTransferEvent(this.transferId, "FTP_TRANSFER_COMPLETED", "FTP 전송이 성공적으로 완료되었습니다"));
    }

    public void fail(String errorMessage) {
        this.transferStatus = FtpTransferStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.addEvent(new FtpTransferEvent(this.transferId, "FTP_TRANSFER_FAILED", "FTP 전송 실패: " + errorMessage));
    }

    public void retry() {
        if (!this.transferStatus.canRetry()) {
            throw new IllegalStateException("재시도할 수 없는 상태입니다: " + this.transferStatus);
        }
        if (this.retryCount >= this.maxRetries) {
            throw new IllegalStateException("최대 재시도 횟수를 초과했습니다: " + this.maxRetries);
        }
        this.retryCount++;
        this.transferStatus = FtpTransferStatus.RETRYING;
        this.errorMessage = null;
        this.addEvent(new FtpTransferEvent(this.transferId, "FTP_TRANSFER_RETRY", "FTP 전송 재시도 (" + this.retryCount + "/" + this.maxRetries + ")"));
    }

    public void cancel() {
        if (this.transferStatus.isTerminal()) {
            throw new IllegalStateException("이미 종료된 전송입니다: " + this.transferStatus);
        }
        this.transferStatus = FtpTransferStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
        this.addEvent(new FtpTransferEvent(this.transferId, "FTP_TRANSFER_CANCELLED", "FTP 전송이 취소되었습니다"));
    }

    public boolean canRetry() {
        return this.transferStatus.canRetry() && this.retryCount < this.maxRetries;
    }

    private void addEvent(FtpTransferEvent event) {
        this.eventList.add(event);
    }

    public List<FtpTransferEvent> getEvents() {
        return new ArrayList<>(eventList);
    }

    public void clearEvents() {
        this.eventList.clear();
    }

    public void apply(Consumer<FtpTransfer> operation) {
        operation.accept(this);
    }
}
