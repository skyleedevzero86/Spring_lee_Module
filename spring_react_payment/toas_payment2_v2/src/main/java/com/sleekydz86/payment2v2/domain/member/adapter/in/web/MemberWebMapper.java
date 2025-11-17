package com.sleekydz86.payment2v2.domain.member.adapter.in.web;

import com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto.*;
import com.sleekydz86.payment2v2.domain.member.application.dto.*;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse;
import com.sleekydz86.payment2v2.global.dto.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public LoginCommand toCommand(LoginRequest request) {
        return LoginCommand.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    public LoginApiResponse toApiResponse(LoginResponse response) {
        LoginApiResponse.LoginData data = LoginApiResponse.LoginData.builder()
                .userId(response.getData().getUserId())
                .email(response.getData().getEmail())
                .name(response.getData().getName())
                .role(response.getData().getRole())
                .token(response.getData().getToken())
                .build();
        
        return LoginApiResponse.builder()
                .message(response.getMessage())
                .data(data)
                .build();
    }

    public PageApiResponse<SearchMemberApiResponse> toPageApiResponse(PageResponse<SearchMemberResponse> pageResponse) {
        List<SearchMemberApiResponse> content = pageResponse.getContent().stream()
                .map(this::toApiResponse)
                .toList();

        return PageApiResponse.<SearchMemberApiResponse>builder()
                .content(content)
                .page(pageResponse.getPage())
                .size(pageResponse.getSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .hasNext(pageResponse.isHasNext())
                .hasPrevious(pageResponse.isHasPrevious())
                .build();
    }
}

