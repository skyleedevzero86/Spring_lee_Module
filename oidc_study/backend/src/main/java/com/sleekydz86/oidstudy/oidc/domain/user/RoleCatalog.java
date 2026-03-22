package com.sleekydz86.oidstudy.oidc.domain.user;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RoleCatalog {

    public static final String USER = "ROLE_USER";
    public static final String MANAGER = "ROLE_MANAGER";
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String PENDING = "ROLE_PENDING";
    public static final String REJECTED = "ROLE_REJECTED";
    public static final String WITHDRAWN = "ROLE_WITHDRAWN";
    public static final String SIGNUP_REQUIRED = "ROLE_SIGNUP_REQUIRED";

    private RoleCatalog() {
    }

    public static Map<String, String> defaults() {
        Map<String, String> roles = new LinkedHashMap<>();
        roles.put(USER, "Approved member");
        roles.put(MANAGER, "Operational manager");
        roles.put(ADMIN, "Application administrator");
        return roles;
    }

    public static Set<String> assignableRoles() {
        return defaults().keySet();
    }

    public static List<String> defaultApprovedRoles() {
        return List.of(USER);
    }
}