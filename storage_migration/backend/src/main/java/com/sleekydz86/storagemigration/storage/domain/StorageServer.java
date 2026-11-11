package com.sleekydz86.storagemigration.storage.domain;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.util.Require;
import com.sleekydz86.storagemigration.global.util.Texts;
import java.time.Instant;

public class StorageServer {

    public static final String DEFAULT_REGION = "us-east-1";

    private final String id;
    private String name;
    private StorageType type;
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private StorageStatus status;
    private String lastMessage;
    private Instant lastCheckedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private StorageServer(String id, String name, StorageType type, String endpoint, String region, String bucket,
                          String accessKey, String secretKey, StorageStatus status, String lastMessage,
                          Instant lastCheckedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.endpoint = endpoint;
        this.region = region;
        this.bucket = bucket;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.status = status;
        this.lastMessage = lastMessage;
        this.lastCheckedAt = lastCheckedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StorageServer register(String id, String name, StorageType type, String endpoint, String region,
                                         String bucket, String accessKey, String secretKey, Instant now) {
        StorageServer server = new StorageServer(Require.notBlank(id, "id"), null, null, null, null, null, null, null,
                StorageStatus.UNTESTED, null, null, now, now);
        server.apply(name, type, endpoint, region, bucket, accessKey, secretKey);
        return server;
    }

    public static StorageServer restore(String id, String name, StorageType type, String endpoint, String region,
                                        String bucket, String accessKey, String secretKey, StorageStatus status,
                                        String lastMessage, Instant lastCheckedAt, Instant createdAt,
                                        Instant updatedAt) {
        return new StorageServer(id, name, type, endpoint, region, bucket, accessKey, secretKey, status, lastMessage,
                lastCheckedAt, createdAt, updatedAt);
    }

    public void changeSettings(String name, StorageType type, String endpoint, String region, String bucket,
                               String accessKey, String newSecretKey, Instant now) {
        String secret = Texts.hasText(newSecretKey) ? newSecretKey : this.secretKey;
        String access = Texts.hasText(accessKey) ? accessKey : this.accessKey;
        apply(name, type, endpoint, region, bucket, access, secret);
        this.status = StorageStatus.UNTESTED;
        this.lastMessage = null;
        this.updatedAt = now;
    }

    public void recordSuccess(String message, Instant at) {
        this.status = StorageStatus.CONNECTED;
        this.lastMessage = message;
        this.lastCheckedAt = at;
        this.updatedAt = at;
    }

    public void recordFailure(String message, Instant at) {
        this.status = StorageStatus.FAILED;
        this.lastMessage = message;
        this.lastCheckedAt = at;
        this.updatedAt = at;
    }

    public boolean pathStyleAccess() {
        return type == StorageType.MINIO;
    }

    public boolean hasCustomEndpoint() {
        return Texts.hasText(endpoint);
    }

    private void apply(String name, StorageType type, String endpoint, String region, String bucket, String accessKey,
                       String secretKey) {
        StorageType checkedType = Require.notNull(type, "스토리지 종류");
        String checkedEndpoint = Texts.trimToNull(endpoint);
        if (checkedType.requiresEndpoint() && checkedEndpoint == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "MinIO는 접속 주소(endpoint)가 필요합니다.");
        }
        if (checkedEndpoint != null && !checkedEndpoint.startsWith("http://") && !checkedEndpoint.startsWith("https://")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "접속 주소는 http:// 또는 https:// 로 시작해야 합니다.");
        }
        String checkedRegion = Texts.trimToNull(region);
        if (checkedType == StorageType.S3 && checkedRegion == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "S3는 리전(region)이 필요합니다.");
        }
        this.name = Require.maxLength(Require.notBlank(name, "서버 이름"), 100, "서버 이름");
        this.type = checkedType;
        this.endpoint = checkedEndpoint == null ? null : checkedEndpoint.replaceAll("/+$", "");
        this.region = checkedRegion == null ? DEFAULT_REGION : checkedRegion;
        this.bucket = validBucket(Require.notBlank(bucket, "버킷"));
        this.accessKey = Require.notBlank(accessKey, "아이디(Access Key)");
        this.secretKey = Require.notBlank(secretKey, "비밀번호(Secret Key)");
    }

    private static String validBucket(String bucket) {
        if (!bucket.matches("[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "버킷 이름은 소문자·숫자·점·하이픈 3~63자여야 합니다.");
        }
        return bucket;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public StorageType type() {
        return type;
    }

    public String endpoint() {
        return endpoint;
    }

    public String region() {
        return region;
    }

    public String bucket() {
        return bucket;
    }

    public String accessKey() {
        return accessKey;
    }

    public String secretKey() {
        return secretKey;
    }

    public StorageStatus status() {
        return status;
    }

    public String lastMessage() {
        return lastMessage;
    }

    public Instant lastCheckedAt() {
        return lastCheckedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
