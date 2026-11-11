package com.sleekydz86.storagemigration.global.response;

import com.sleekydz86.storagemigration.global.error.ErrorResponse;

public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}
