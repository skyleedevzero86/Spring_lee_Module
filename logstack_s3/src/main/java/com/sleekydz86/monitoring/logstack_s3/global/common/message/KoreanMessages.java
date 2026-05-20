package com.sleekydz86.monitoring.logstack_s3.global.common.message;

public final class KoreanMessages {

    private KoreanMessages() {
    }

    public static final String UPLOAD_COMPLETE = "업로드가 완료되었습니다.";
    public static final String SEED_COMPLETE = "데모 데이터 %d건이 등록되었습니다.";
    public static final String INTERNAL_ERROR = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
    public static final String FILE_TOO_LARGE = "업로드 파일 크기가 허용 범위를 초과했습니다.";
    public static final String INVALID_REQUEST = "잘못된 요청입니다.";

    public static String seedComplete(int count) {
        return SEED_COMPLETE.formatted(count);
    }
}
