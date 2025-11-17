package com.sleekydz86.payment2v2.domain.member.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String message;
    private LoginData data;
    
    @Getter
    @Builder
    public static class LoginData {
        private Long userId;
        private String email;
        private String name;
        private String role;
        private String token;
    }
}

