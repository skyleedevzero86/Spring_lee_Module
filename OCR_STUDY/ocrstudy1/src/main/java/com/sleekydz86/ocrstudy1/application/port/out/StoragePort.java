package com.sleekydz86.ocrstudy1.application.port.out;

import java.io.InputStream;

public interface StoragePort {
    String uploadFile(InputStream inputStream, String filename, String contentType, long size);
    InputStream downloadFile(String objectName);
    void deleteFile(String objectName);
    String getFileUrl(String objectName);
}