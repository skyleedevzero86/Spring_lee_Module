package com.sleekydz86.monitoring.logstack_s3.domain.exception;

import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String id) {
        super(DomainMessages.fileNotFound(id));
    }
}
