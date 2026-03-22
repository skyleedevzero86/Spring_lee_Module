package com.sleekydz86.oidstudy.oidc.web.resp;

import com.sleekydz86.oidstudy.oidc.domain.AccountStatus;
import java.time.LocalDateTime;
import java.util.List;

public record SessionAccountResponse(
        Long id,
        String provider,
        String providerUserId,
        String loginId,
        String email,
        String displayName,
        String nickname,
        String contactNumber,
        String profileImageUrl,
        AccountStatus status,
        List<String> roles,
        boolean active,
        boolean admin,
        boolean registrationRequired,
        boolean withdrawn,
        boolean canWithdraw,
        LocalDateTime termsAgreedAt,
        LocalDateTime approvedAt,
        LocalDateTime withdrawnAt,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}