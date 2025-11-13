package com.sleekydz86.payment2v2.domain.member.adapter.in.web;

import com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto.*;
import com.sleekydz86.payment2v2.domain.member.application.dto.*;
import org.springframework.stereotype.Component;

@Component
public class MemberWebMapper {

    public RegisterMemberCommand toCommand(RegisterMemberRequest request) {
        return RegisterMemberCommand.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .build();
    }

    public RegisterMemberApiResponse toApiResponse(RegisterMemberResponse response) {
        return RegisterMemberApiResponse.builder()
                .id(response.getId())
                .email(response.getEmail())
                .name(response.getName())
                .role(response.getRole())
                .build();
    }

    public FindMemberApiResponse toApiResponse(FindMemberResponse response) {
        return FindMemberApiResponse.builder()
                .id(response.getId())
                .email(response.getEmail())
                .name(response.getName())
                .role(response.getRole())
                .build();
    }

    public SearchMemberApiResponse toApiResponse(SearchMemberResponse response) {
        return SearchMemberApiResponse.builder()
                .id(response.getId())
                .email(response.getEmail())
                .name(response.getName())
                .role(response.getRole())
                .build();
    }

    public ResetPasswordCommand toCommand(ResetPasswordRequest request) {
        return ResetPasswordCommand.builder()
                .email(request.getEmail())
                .newPassword(request.getNewPassword())
                .build();
    }

    public ResetPasswordApiResponse toApiResponse(ResetPasswordResponse response) {
        return ResetPasswordApiResponse.builder()
                .message(response.getMessage())
                .email(response.getEmail())
                .build();
    }
}

