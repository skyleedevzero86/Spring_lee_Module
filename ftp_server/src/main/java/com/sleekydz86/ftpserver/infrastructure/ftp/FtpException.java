package com.sleekydz86.ftpserver.infrastructure.ftp;

public class FtpException extends Exception {
    public FtpException(String message) {
        super(message);
    }

    public FtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
