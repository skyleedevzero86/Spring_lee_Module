package com.sleekydz86.monitoring.logstack_s3.common.message;

public final class KoreanMessages {

    private KoreanMessages() {
    }

    public static final String FILE_NOT_FOUND = "파일을 찾을 수 없습니다. (id=%s)";
    public static final String ID_REQUIRED_FOR_UPDATE = "수정하려면 파일 id가 필요합니다.";
    public static final String FILE_READ_FAILED = "파일을 읽는 중 오류가 발생했습니다.";
    public static final String THUMBNAIL_CREATE_FAILED = "썸네일을 만드는 중 오류가 발생했습니다.";
    public static final String SEED_COUNT_MIN = "시드 건수는 1 이상이어야 합니다.";
    public static final String SEED_COUNT_MAX = "시드 건수는 500000 이하여야 합니다.";
    public static final String UPLOAD_COMPLETE = "업로드가 완료되었습니다.";
    public static final String SEED_COMPLETE = "데모 데이터 %d건이 등록되었습니다.";
    public static final String INTERNAL_ERROR = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
    public static final String FILE_TOO_LARGE = "업로드 파일 크기가 허용 범위를 초과했습니다.";
    public static final String INVALID_REQUEST = "잘못된 요청입니다.";

    public static String fileNotFound(String id) {
        return FILE_NOT_FOUND.formatted(id);
    }

    public static String seedComplete(int count) {
        return SEED_COMPLETE.formatted(count);
    }
}
