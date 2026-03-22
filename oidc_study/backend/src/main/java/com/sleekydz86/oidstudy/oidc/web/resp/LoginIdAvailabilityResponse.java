package com.sleekydz86.oidstudy.oidc.web.resp;

import com.sleekydz86.oidstudy.oidc.application.user.LoginIdCheckStatus;

public record LoginIdAvailabilityResponse(
        boolean available,
        LoginIdCheckStatus status,
        String message
) {
}