package com.sleekydz86.toaspayment.application.dto;

public record LoginResponse(
        String message,
        LoginData data
) {
    public record LoginData(
            Long userId,
            String email,
            String name,
            String role,
            String token
    ) {
    }
}
