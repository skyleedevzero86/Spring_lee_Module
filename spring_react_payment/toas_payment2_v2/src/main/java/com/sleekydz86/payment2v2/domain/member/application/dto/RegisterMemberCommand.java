package com.sleekydz86.payment2v2.domain.member.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterMemberCommand {
    private String email;
    private String password;
    private String name;
}
