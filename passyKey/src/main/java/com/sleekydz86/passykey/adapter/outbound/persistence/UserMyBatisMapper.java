package com.sleekydz86.passykey.adapter.outbound.persistence;

import com.sleekydz86.passykey.domain.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserMyBatisMapper {

    void save(Map<String, Object> params);

    User selectById(Long id);

    User selectByUsername(String username);

    User selectByEmail(String email);

    User selectByUserHandle(String userHandle);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
