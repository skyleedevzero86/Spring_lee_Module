package com.sleekydz86.toaspayment.application.dto;

public record RegisterResponse(
        String message,
        RegisterData data
) {
    public record RegisterData(
            Long userId,
            String email,
            String name
    ) {
    }
}


