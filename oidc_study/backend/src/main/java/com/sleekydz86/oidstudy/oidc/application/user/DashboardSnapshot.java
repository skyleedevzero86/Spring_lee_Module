package com.sleekydz86.oidstudy.oidc.application.user;

import com.sleekydz86.oidstudy.oidc.domain.UserAccount;

public record DashboardSnapshot(
        long totalUsers,
        long activeUsers,
        long pendingUsers,
        long rejectedUsers,
        long withdrawnUsers,
        UserAccount currentUser
) {
}