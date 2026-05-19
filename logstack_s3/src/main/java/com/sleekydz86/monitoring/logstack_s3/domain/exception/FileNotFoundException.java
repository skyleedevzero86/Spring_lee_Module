package com.sleekydz86.monitoring.logstack_s3.domain.exception;

import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String id) {
        super(KoreanMessages.fileNotFound(id));
    }
}
