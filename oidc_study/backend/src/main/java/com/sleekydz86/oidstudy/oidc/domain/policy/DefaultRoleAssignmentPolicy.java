package com.sleekydz86.oidstudy.oidc.domain.policy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.sleekydz86.oidstudy.oidc.domain.RoleCatalog;
import org.springframework.stereotype.Component;

@Component
public class DefaultRoleAssignmentPolicy implements RoleAssignmentPolicy {

    @Override
    public List<String> normalize(List<String> requestedRoles) {
        return Stream.concat(
                        Stream.of(RoleCatalog.USER),
                        Optional.ofNullable(requestedRoles).orElse(List.of()).stream()
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(String::toUpperCase)
                .filter(RoleCatalog.assignableRoles()::contains)
                .distinct()
                .toList();
    }
}