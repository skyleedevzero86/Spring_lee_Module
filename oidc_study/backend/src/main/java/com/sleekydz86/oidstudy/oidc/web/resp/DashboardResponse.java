package com.sleekydz86.oidstudy.oidc.web.resp;

import java.util.List;

public record DashboardResponse(
        long totalUsers,
        long activeUsers,
        long pendingUsers,
        long rejectedUsers,
        long withdrawnUsers,
        List<String> myRoles,
        String myStatus
) {
}