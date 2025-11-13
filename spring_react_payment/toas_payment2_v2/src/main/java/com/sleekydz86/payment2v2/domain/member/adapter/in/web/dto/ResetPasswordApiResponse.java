package com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordApiResponse {
    private String message;
    private String email;
}

