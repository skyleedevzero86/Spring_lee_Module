package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.minio;

import com.sleekydz86.ocrstudy1.application.port.out.StoragePort;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.BucketExistsArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class MinIOStorageAdapter implements StoragePort {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinIOStorageAdapter(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        initializeBucket();
    }

    private void initializeBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize bucket: {}", bucketName, e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String filename, String contentType, long size) {
        try {
            String objectName = generateObjectName(filename);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            log.info("Uploaded file: {} as {}", filename, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Failed to upload file: {}", filename, e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to download file: {}", objectName, e);
            throw new RuntimeException("File download failed", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("Deleted file: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectName, e);
            throw new RuntimeException("File delete failed", e);
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return String.format("%s/%s/%s",
                System.getProperty("minio.endpoint", "http://localhost:9000"),
                bucketName,
                objectName);
    }

    private String generateObjectName(String filename) {
        return System.currentTimeMillis() + "_" + filename;
    }
}