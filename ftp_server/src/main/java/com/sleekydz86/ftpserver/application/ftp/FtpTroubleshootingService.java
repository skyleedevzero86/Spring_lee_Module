package com.sleekydz86.ftpserver.application.ftp;

import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferRepository;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferStatus;
import com.sleekydz86.ftpserver.infrastructure.ftp.FtpClient;
import com.sleekydz86.ftpserver.infrastructure.ftp.FtpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FtpTroubleshootingService {
    
    private final FtpTransferRepository repository;
    private final FtpClient ftpClient;

    @Async("ftpExecutor")
    @Transactional
    public void retryTransfer(FtpTransferId transferId) {
        repository.findById(transferId)
            .filter(FtpTransfer::canRetry)
            .ifPresentOrElse(
                this::executeRetry,
                () -> log.warn("재시도할 수 없는 전송입니다: {}", transferId)
            );
    }

    private void executeRetry(FtpTransfer transfer) {
        try {
            transfer.retry();
            repository.save(transfer);
            log.info("FTP 전송 재시도 시작: {}, 재시도 횟수: {}/{}", 
                    transfer.getTransferId(), transfer.getRetryCount(), transfer.getMaxRetries());
            
            ftpClient.uploadFile(
                transfer.getRemotePath(),
                transfer.getFileName(),
                transfer.getFileContent()
            );
            
            transfer.complete();
            repository.save(transfer);
            log.info("FTP 전송 재시도 성공: {}", transfer.getTransferId());
            
        } catch (FtpException e) {
            handleRetryFailure(transfer, e);
        } catch (Exception e) {
            handleRetryFailure(transfer, new FtpException("예상치 못한 오류: " + e.getMessage(), e));
        }
    }

    private void handleRetryFailure(FtpTransfer transfer, FtpException e) {
        log.error("FTP 전송 재시도 실패: {}", transfer.getTransferId(), e);
        transfer.fail("재시도 실패: " + e.getMessage());
        repository.save(transfer);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedTransfers() {
        List<FtpTransfer> failedTransfers = repository.findFailedTransfers();
        if (failedTransfers.isEmpty()) {
            return;
        }
        log.info("실패한 FTP 전송 자동 재시도 시작: {}건", failedTransfers.size());
        failedTransfers.stream()
            .filter(FtpTransfer::canRetry)
            .forEach(transfer -> retryTransfer(transfer.getTransferId()));
    }

    public boolean testConnection() {
        log.info("FTP 연결 테스트 시작");
        boolean result = ftpClient.testConnection();
        log.info("FTP 연결 테스트 결과: {}", result ? "성공" : "실패");
        return result;
    }

    @Transactional(readOnly = true)
    public FtpTransferStatistics getStatistics() {
        List<FtpTransfer> allTransfers = getAllTransfers();
        long total = allTransfers.size();
        long success = countByStatus(FtpTransferStatus.SUCCESS);
        long failed = countByStatus(FtpTransferStatus.FAILED);
        long pending = countByStatus(FtpTransferStatus.PENDING);
        long inProgress = countByStatus(FtpTransferStatus.IN_PROGRESS);
        long retrying = countByStatus(FtpTransferStatus.RETRYING);

        return new FtpTransferStatistics(total, success, failed, pending, inProgress, retrying);
    }

    private List<FtpTransfer> getAllTransfers() {
        return List.of(
            repository.findByStatus(FtpTransferStatus.PENDING),
            repository.findByStatus(FtpTransferStatus.IN_PROGRESS),
            repository.findByStatus(FtpTransferStatus.SUCCESS),
            repository.findByStatus(FtpTransferStatus.FAILED),
            repository.findByStatus(FtpTransferStatus.RETRYING)
        ).stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
    }

    private long countByStatus(FtpTransferStatus status) {
        return repository.findByStatus(status).size();
    }

    public static class FtpTransferStatistics {
        private final long total;
        private final long success;
        private final long failed;
        private final long pending;
        private final long inProgress;
        private final long retrying;

        public FtpTransferStatistics(long total, long success, long failed, 
                                    long pending, long inProgress, long retrying) {
            this.total = total;
            this.success = success;
            this.failed = failed;
            this.pending = pending;
            this.inProgress = inProgress;
            this.retrying = retrying;
        }

        public long getTotal() { return total; }
        public long getSuccess() { return success; }
        public long getFailed() { return failed; }
        public long getPending() { return pending; }
        public long getInProgress() { return inProgress; }
        public long getRetrying() { return retrying; }
        
        public double getSuccessRate() {
            return total > 0 ? (double) success / total * 100 : 0;
        }
    }
}
