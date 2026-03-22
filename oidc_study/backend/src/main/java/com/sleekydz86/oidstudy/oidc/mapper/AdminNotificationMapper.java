package com.sleekydz86.oidstudy.oidc.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminNotificationMapper {

    void insert(AdminNotification notification);

    List<AdminNotification> findRecent(@Param("limit") int limit);
}