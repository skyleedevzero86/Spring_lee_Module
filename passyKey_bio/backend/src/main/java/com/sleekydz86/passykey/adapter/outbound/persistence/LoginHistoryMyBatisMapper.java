package com.sleekydz86.passykey.adapter.outbound.persistence;

import com.sleekydz86.passykey.domain.model.LoginHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LoginHistoryMyBatisMapper {
    void insert(Map<String, Object> params);

    LoginHistory selectActiveSessionByUserId(@Param("userId") Long userId);

    LoginHistory selectBySessionId(@Param("sessionId") String sessionId);

    List<LoginHistory> selectByUserIdOrderByLoginAtDesc(@Param("userId") Long userId, @Param("limit") int limit);

    void updateLogoutAt(Map<String, Object> params);

    List<LoginHistory> selectAllActiveSessions();
}
