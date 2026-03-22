package com.sleekydz86.oidstudy.oidc.application.user;

public record LoginIdCheckResult(
        boolean available,
        LoginIdCheckStatus status,
        String message
) {
}