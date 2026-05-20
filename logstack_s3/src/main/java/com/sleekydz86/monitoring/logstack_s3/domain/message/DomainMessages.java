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

    public static String fileNotFound(String id) {
        return FILE_NOT_FOUND.formatted(id);
    }
}
