package com.sleekydz86.monitoring.logstack_s3.domain.exception;

public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStorageException(String message) {
        super(message);
    }
}
