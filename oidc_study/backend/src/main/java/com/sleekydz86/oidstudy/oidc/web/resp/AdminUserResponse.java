package com.sleekydz86.oidstudy.oidc.web.resp;

import com.sleekydz86.oidstudy.oidc.domain.AccountStatus;
import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String loginId,
        String email,
        String displayName,
        String nickname,
        String contactNumber,
        String provider,
        String providerUserId,
        AccountStatus status,
        List<String> roles,
        LocalDateTime createdAt,
        LocalDateTime termsAgreedAt,
        LocalDateTime approvedAt,
        LocalDateTime withdrawnAt,
        String withdrawalReason,
        LocalDateTime lastLoginAt
) {
}