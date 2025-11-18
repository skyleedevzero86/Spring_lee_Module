package com.sleekydz86.payment2v2.domain.member.port.out;

public interface PasswordEncoder {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}




