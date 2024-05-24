package com.example.application.security;

import org.springframework.security.core.GrantedAuthority;

public final class Roles {

    private Roles() {
    }

    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";

    static boolean isRole(GrantedAuthority authority) {
        return authority.getAuthority().startsWith("ROLE_");
    }

    static String toRoleName(GrantedAuthority authority) {
        return authority.getAuthority().substring(5);
    }
}
