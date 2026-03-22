package com.sleekydz86.oidstudy.oidc.domain.user.repository;

import java.util.List;

public interface RoleCatalogRepository {
    void ensureDefaultCatalog();
    List<String> findRolesByUserId(Long userId);
    void replaceUserRoles(Long userId, List<String> roles);
}