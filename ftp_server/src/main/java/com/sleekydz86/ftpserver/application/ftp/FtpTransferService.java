package com.sleekydz86.ftpserver.application.ftp;

import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferRepository;
import com.sleekydz86.ftpserver.infrastructure.ftp.FtpClient;
import com.sleekydz86.ftpserver.infrastructure.ftp.FtpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class FtpTransferService {
    
    private final FtpTransferRepository repository;
    private final FtpClient ftpClient;

    @Transactional
    public FtpTransferId requestTransfer(String paymentRequestNo, String remotePath, String fileName, byte[] fileContent) {
        log.info("FTP 전송 요청 생성: paymentRequestNo={}, fileName={}", paymentRequestNo, fileName);
        FtpTransfer transfer = FtpTransfer.create(paymentRequestNo, remotePath, fileName, fileContent);
        repository.save(transfer);
        transferAsync(transfer.getTransferId());
        return transfer.getTransferId();
    }

    @Async("ftpExecutor")
    @Transactional
    public void transferAsync(FtpTransferId transferId) {
        repository.findById(transferId)
            .ifPresentOrElse(
                transfer -> executeTransfer(transfer),
                () -> log.error("FTP 전송을 찾을 수 없습니다: {}", transferId)
            );
    }

    private void executeTransfer(FtpTransfer transfer) {
        try {
            transfer.start();
            repository.save(transfer);
            log.info("FTP 전송 시작: {}", transfer.getTransferId());
            
            ftpClient.uploadFile(
                transfer.getRemotePath(),
                transfer.getFileName(),
                transfer.getFileContent()
            );
            
            transfer.complete();
            repository.save(transfer);
            log.info("FTP 전송 완료: {}", transfer.getTransferId());
            
        } catch (FtpException e) {
            handleTransferFailure(transfer, e);
        } catch (Exception e) {
            handleTransferFailure(transfer, new FtpException("예상치 못한 오류: " + e.getMessage(), e));
        }
    }

    private void handleTransferFailure(FtpTransfer transfer, FtpException e) {
        log.error("FTP 전송 실패: {}", transfer.getTransferId(), e);
        transfer.fail(e.getMessage());
        repository.save(transfer);
    }

    @Transactional(readOnly = true)
    public Optional<FtpTransfer> getTransfer(FtpTransferId transferId) {
        return repository.findById(transferId);
    }

    @Transactional(readOnly = true)
    public List<FtpTransfer> getTransfersByPaymentRequestNo(String paymentRequestNo) {
        return repository.findByPaymentRequestNo(paymentRequestNo);
    }
}
