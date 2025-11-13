package com.sleekydz86.payment2v2.domain.member.model;

public interface PasswordEncoder {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

