package com.sleekydz86.ftpserver.application.ftp;

import lombok.Data;

@Data
public class FtpTransferCommand {
    private String paymentRequestNo;
    private String remotePath;
    private String fileName;
    private byte[] fileContent;
}
