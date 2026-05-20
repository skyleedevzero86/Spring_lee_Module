package com.sleekydz86.monitoring.logstack_s3.application.port;

import java.io.InputStream;
import java.util.List;

import com.sleekydz86.monitoring.logstack_s3.domain.model.ListedStorageObject;

public interface ObjectStoragePort {

    void put(String key, String contentType, long size, InputStream body);

    void putBytes(String key, String contentType, byte[] body);

    String presignPreview(String key);

    String presignDownload(String key, String filename);

    void delete(String key);

    boolean exists(String key);

    String bucketName();

    List<ListedStorageObject> listObjects();
}
