package com.sleekydz86.ftpserver.infrastructure.ftp;

import com.sleekydz86.ftpserver.domain.ftp.FtpTransfer;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferId;
import com.sleekydz86.ftpserver.domain.ftp.FtpTransferStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FtpTransferMapper {
    void processFtpTransfer(java.util.Map<String, Object> params);

    FtpTransfer selectFtpTransfer(@Param("transferId") String transferId);
    List<FtpTransfer> selectFtpTransfersByPaymentRequestNo(@Param("paymentRequestNo") String paymentRequestNo);
    List<FtpTransfer> selectFtpTransfersByStatus(@Param("transferStatus") String transferStatus);
    List<FtpTransfer> selectFailedFtpTransfers();
}
