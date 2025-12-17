package com.sleekydz86.sever.model.infrastructure.persistence;

import com.sleekydz86.sever.model.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    
    User findByUsername(String username);

    List<String> findAuthoritiesByUsername(String username);

    Long findUserIdByUsername(String username);

    void insertUser(String username, String password, boolean enabled);

    void insertAuthority(Long userId, String authority);

    void executeUserProcedure(@Param("operation") String operation,
                               @Param("id") Long id,
                               @Param("username") String username,
                               @Param("password") String password,
                               @Param("enabled") Boolean enabled,
                               @Param("authority") String authority);

    User findUserById(Long id);

    User findUserByIdOrUsername(@Param("id") Long id, @Param("username") String username);

    List<Map<String, Object>> findAllUsersFromView();

    List<Map<String, Object>> searchUsersFromView(@Param("keyword") String keyword);
}
