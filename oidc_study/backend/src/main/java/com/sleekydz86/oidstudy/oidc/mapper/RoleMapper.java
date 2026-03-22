package com.sleekydz86.oidstudy.oidc.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper {

    int countByRoleCode(@Param("roleCode") String roleCode);

    void insertRole(@Param("roleCode") String roleCode, @Param("roleName") String roleName);

    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    void deleteUserRoles(@Param("userId") Long userId);

    void insertUserRoleByCode(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}