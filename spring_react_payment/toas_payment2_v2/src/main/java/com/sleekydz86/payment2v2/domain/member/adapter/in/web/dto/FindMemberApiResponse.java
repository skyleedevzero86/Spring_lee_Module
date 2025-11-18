package com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindMemberApiResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
}
