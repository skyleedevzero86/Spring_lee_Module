package com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterMemberApiResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
}

