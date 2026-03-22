package com.sleekydz86.oidstudy.oidc.domain.notification.repository;

import com.sleekydz86.oidstudy.oidc.domain.notification.AdminNotification;
import java.util.List;

public interface AdminNotificationRepository {

    AdminNotification save(AdminNotification notification);

    List<AdminNotification> findRecent(int limit);
}