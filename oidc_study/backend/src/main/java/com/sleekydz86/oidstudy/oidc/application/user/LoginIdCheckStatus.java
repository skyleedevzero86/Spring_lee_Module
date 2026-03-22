package com.sleekydz86.oidstudy.oidc.application.user;

public enum LoginIdCheckStatus {
    AVAILABLE,
    AVAILABLE_CURRENT_USER,
    EXISTING_MEMBER,
    WITHDRAWN_MEMBER,
    INVALID
}