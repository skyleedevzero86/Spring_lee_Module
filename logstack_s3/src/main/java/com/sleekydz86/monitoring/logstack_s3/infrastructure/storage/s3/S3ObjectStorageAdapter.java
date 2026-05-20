package com.sleekydz86.monitoring.logstack_s3.infrastructure.storage.s3;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.domain.model.ListedStorageObject;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3ObjectStorageAdapter implements ObjectStoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presign-duration-minutes:60}")
    private long presignDurationMinutes;

    @Override
    public void put(String key, String contentType, long size, InputStream body) {
        ensureBucket();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(body, size));
    }

    @Override
    public void putBytes(String key, String contentType, byte[] body) {
        ensureBucket();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength((long) body.length)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(body));
    }

    @Override
    public String presignPreview(String key) {
        return presignPreview(bucket, key);
    }

    @Override
    public String presignPreview(String bucketName, String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public String presignDownload(String key, String filename) {
        if (key == null || key.isBlank()) {
            return null;
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition("attachment; filename=\"" + filename + "\"")
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        ensureBucket();
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    @Override
    public String bucketName() {
        return bucket;
    }

    @Override
    public List<ListedStorageObject> listObjects() {
        return listObjects(bucket);
    }

    @Override
    public List<ListedStorageObject> listObjects(String bucketName) {
        ensureBucket(bucketName);
        List<ListedStorageObject> objects = new ArrayList<>();
        String continuationToken = null;
        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .maxKeys(1000);
            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }
            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
            for (S3Object object : response.contents()) {
                objects.add(new ListedStorageObject(
                        object.key(),
                        object.size(),
                        object.lastModified()));
            }
            continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
        } while (continuationToken != null);
        return objects;
    }

    @Override
    public Optional<String> findFirstObjectKey(String bucketName, String keyPrefix) {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            return Optional.empty();
        }
        ensureBucket(bucketName);
        ListObjectsV2Response response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(keyPrefix)
                .maxKeys(1)
                .build());
        return response.contents().stream()
                .map(S3Object::key)
                .findFirst();
    }

    @Override
    public boolean exists(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        ensureBucket();
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    private void ensureBucket() {
        ensureBucket(bucket);
    }

    private void ensureBucket(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }
}
