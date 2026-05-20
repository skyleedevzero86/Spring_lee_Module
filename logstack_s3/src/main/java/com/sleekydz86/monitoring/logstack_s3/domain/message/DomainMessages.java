package com.sleekydz86.monitoring.logstack_s3.domain.message;

public final class DomainMessages {

    private DomainMessages() {
    }

    public static final String FILE_NOT_FOUND = "파일을 찾을 수 없습니다. id=%s";
    public static final String ID_REQUIRED_FOR_UPDATE = "수정하려면 파일 id가 필요합니다.";
    public static final String SEED_COUNT_MIN = "시드 건수는 1 이상이어야 합니다.";
    public static final String SEED_COUNT_MAX = "시드 건수는 500000 이하여야 합니다.";
    public static final String ADMIN_API_KEY_REQUIRED = "관리 API 키가 필요합니다.";
    public static final String ADMIN_API_KEY_INVALID = "관리 API 키가 올바르지 않습니다.";
    public static final String BUCKET_NOT_FOUND = "스토리지 버킷을 찾을 수 없습니다. code=%s";
    public static final String STORAGE_OBJECT_NOT_FOUND = "S3 객체를 찾을 수 없습니다. key=%s";

    public static String bucketNotFound(String bucketCode) {
        return BUCKET_NOT_FOUND.formatted(bucketCode);
    }

    public static String storageObjectNotFound(String key) {
        return STORAGE_OBJECT_NOT_FOUND.formatted(key);
    }

    public static String fileNotFound(String id) {
        return FILE_NOT_FOUND.formatted(id);
    }
}
