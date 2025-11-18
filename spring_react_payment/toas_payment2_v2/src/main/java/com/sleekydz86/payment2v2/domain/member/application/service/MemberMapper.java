package com.sleekydz86.payment2v2.domain.member.application.service;

import com.sleekydz86.payment2v2.domain.member.application.dto.FindMemberResponse;
import com.sleekydz86.payment2v2.domain.member.application.dto.LoginResponse;
import com.sleekydz86.payment2v2.domain.member.application.dto.RegisterMemberResponse;
import com.sleekydz86.payment2v2.domain.member.application.dto.SearchMemberResponse;
import com.sleekydz86.payment2v2.domain.member.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public RegisterMemberResponse toRegisterResponse(Member member) {
        return RegisterMemberResponse.builder()
                .id(member.getId())
                .email(member.getEmailValue())
                .name(member.getNameValue())
                .role(member.getRole().name())
                .build();
    }

    public FindMemberResponse toFindResponse(Member member) {
        return FindMemberResponse.builder()
                .id(member.getId())
                .email(member.getEmailValue())
                .name(member.getNameValue())
                .role(member.getRole().name())
                .build();
    }

    public SearchMemberResponse toSearchResponse(Member member) {
        return SearchMemberResponse.builder()
                .id(member.getId())
                .email(member.getEmailValue())
                .name(member.getNameValue())
                .role(member.getRole().name())
                .build();
    }

    public LoginResponse toLoginResponse(Member member) {
        LoginResponse.LoginData data = LoginResponse.LoginData.builder()
                .userId(member.getId())
                .email(member.getEmailValue())
                .name(member.getNameValue())
                .role(member.getRole().name())
                .token("")
                .build();

        return LoginResponse.builder()
                .message("로그인 성공")
                .data(data)
                .build();
    }
}
