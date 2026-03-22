package com.sleekydz86.oidstudy.oidc.domain.user.policy;

import java.util.List;

public interface RoleAssignmentPolicy {

    List<String> normalize(List<String> requestedRoles);
}