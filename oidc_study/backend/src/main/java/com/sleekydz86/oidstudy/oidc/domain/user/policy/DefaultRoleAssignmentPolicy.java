package com.sleekydz86.oidstudy.oidc.domain.user.policy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.sleekydz86.oidstudy.oidc.domain.user.RoleCatalog;
import org.springframework.stereotype.Component;

@Component
public class DefaultRoleAssignmentPolicy implements RoleAssignmentPolicy {

    @Override
    public List<String> normalize(List<String> requestedRoles) {
        return Optional.ofNullable(requestedRoles).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(String::toUpperCase)
                .filter(RoleCatalog.assignableRoles()::contains)
                .distinct()
                .toList();
    }
}