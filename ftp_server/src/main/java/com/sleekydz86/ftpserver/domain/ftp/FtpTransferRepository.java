package com.sleekydz86.ftpserver.domain.ftp;

import java.util.List;
import java.util.Optional;

public interface FtpTransferRepository {
    FtpTransfer save(FtpTransfer transfer);
    Optional<FtpTransfer> findById(FtpTransferId id);
    List<FtpTransfer> findByPaymentRequestNo(String paymentRequestNo);
    List<FtpTransfer> findByStatus(FtpTransferStatus status);
    List<FtpTransfer> findFailedTransfers();
}
