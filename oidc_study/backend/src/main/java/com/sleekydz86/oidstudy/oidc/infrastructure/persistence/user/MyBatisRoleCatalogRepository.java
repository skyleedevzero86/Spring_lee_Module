package com.sleekydz86.oidstudy.oidc.infrastructure.persistence.user;

import java.util.List;
import java.util.Map;

import com.sleekydz86.oidstudy.oidc.domain.RoleCatalog;
import com.sleekydz86.oidstudy.oidc.domain.repository.RoleCatalogRepository;
import com.sleekydz86.oidstudy.oidc.mapper.RoleMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisRoleCatalogRepository implements RoleCatalogRepository {

    private final RoleMapper roleMapper;

    public MyBatisRoleCatalogRepository(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public void ensureDefaultCatalog() {
        for (Map.Entry<String, String> role : RoleCatalog.defaults().entrySet()) {
            if (roleMapper.countByRoleCode(role.getKey()) == 0) {
                roleMapper.insertRole(role.getKey(), role.getValue());
            }
        }
    }

    @Override
    public List<String> findRolesByUserId(Long userId) {
        return roleMapper.findRoleCodesByUserId(userId);
    }

    @Override
    public void replaceUserRoles(Long userId, List<String> roles) {
        roleMapper.deleteUserRoles(userId);
        for (String role : roles) {
            roleMapper.insertUserRoleByCode(userId, role);
        }
    }
}