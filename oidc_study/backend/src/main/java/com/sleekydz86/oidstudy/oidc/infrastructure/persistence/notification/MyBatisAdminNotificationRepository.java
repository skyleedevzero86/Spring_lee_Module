package com.sleekydz86.oidstudy.oidc.infrastructure.persistence.notification;

import java.util.List;

import com.sleekydz86.oidstudy.oidc.mapper.AdminNotificationMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminNotificationRepository implements AdminNotificationRepository {

    private final AdminNotificationMapper adminNotificationMapper;

    public MyBatisAdminNotificationRepository(AdminNotificationMapper adminNotificationMapper) {
        this.adminNotificationMapper = adminNotificationMapper;
    }

    @Override
    public AdminNotification save(AdminNotification notification) {
        adminNotificationMapper.insert(notification);
        return notification;
    }

    @Override
    public List<AdminNotification> findRecent(int limit) {
        return adminNotificationMapper.findRecent(limit);
    }
}