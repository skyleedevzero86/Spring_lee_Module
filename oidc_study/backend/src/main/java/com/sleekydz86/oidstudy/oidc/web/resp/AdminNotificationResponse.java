package com.sleekydz86.oidstudy.oidc.web.resp;

import java.time.LocalDateTime;

public record AdminNotificationResponse(
        Long id,
        String category,
        String title,
        String message,
        Long targetUserId,
        LocalDateTime createdAt
) {
}