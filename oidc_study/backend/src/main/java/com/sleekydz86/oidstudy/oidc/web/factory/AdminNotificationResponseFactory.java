package com.sleekydz86.oidstudy.oidc.web.factory;

import com.sleekydz86.oidstudy.oidc.web.resp.AdminNotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminNotificationResponseFactory {

    public AdminNotificationResponse create(AdminNotification notification) {
        return new AdminNotificationResponse(
                notification.getId(),
                notification.getCategory(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getTargetUserId(),
                notification.getCreatedAt()
        );
    }
}