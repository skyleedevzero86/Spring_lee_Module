package com.sleekydz86.oidstudy.oidc.mapper;

import java.util.List;
import com.sleekydz86.oidstudy.oidc.domain.notification.AdminNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminNotificationMapper {

    void insert(AdminNotification notification);

    List<AdminNotification> findRecent(@Param("limit") int limit);
}