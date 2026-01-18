package com.sleekydz86.ftpserver.infrastructure.ftp;

import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferRepository;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class FtpTransferRepositoryImpl implements FtpTransferRepository {
    
    private final FtpTransferMapper mapper;

    public FtpTransferRepositoryImpl(FtpTransferMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public FtpTransfer save(FtpTransfer transfer) {
        String operation = determineOperation(transfer);
        Map<String, Object> params = new HashMap<>();
        params.put("inOperation", operation);
        params.put("transferId", transfer.getTransferId().getValue());
        params.put("paymentRequestNo", transfer.getPaymentRequestNo());
        params.put("remotePath", transfer.getRemotePath());
        params.put("fileName", transfer.getFileName());
        params.put("fileContent", transfer.getFileContent());
        params.put("transferStatus", transfer.getTransferStatus().name());
        params.put("errorMessage", transfer.getErrorMessage());
        params.put("retryCount", transfer.getRetryCount());
        params.put("maxRetries", transfer.getMaxRetries());
        params.put("rtnCod", "1");
        params.put("errMsg", "");

        mapper.processFtpTransfer(params);

        return transfer;
    }

    private String determineOperation(FtpTransfer transfer) {
        return findById(transfer.getTransferId())
            .map(t -> "U")
            .orElse("C");
    }

    @Override
    public Optional<FtpTransfer> findById(FtpTransferId id) {
        FtpTransfer transfer = mapper.selectFtpTransfer(id.getValue());
        return Optional.ofNullable(transfer);
    }

    @Override
    public List<FtpTransfer> findByPaymentRequestNo(String paymentRequestNo) {
        return mapper.selectFtpTransfersByPaymentRequestNo(paymentRequestNo);
    }

    @Override
    public List<FtpTransfer> findByStatus(FtpTransferStatus status) {
        return mapper.selectFtpTransfersByStatus(status.name());
    }

    @Override
    public List<FtpTransfer> findFailedTransfers() {
        return mapper.selectFailedFtpTransfers();
    }
}
