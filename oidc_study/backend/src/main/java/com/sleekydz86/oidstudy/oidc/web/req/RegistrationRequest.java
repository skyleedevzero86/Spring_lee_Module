package com.sleekydz86.oidstudy.oidc.web.req;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "아이디를 입력하세요.")
        String loginId,
        @NotBlank(message = "이름을 입력하세요.")
        @Size(max = 100, message = "이름은 100자 이하로 입력하세요.")
        String displayName,
        @NotBlank(message = "연락처를 입력하세요.")
        @Size(max = 30, message = "연락처는 30자 이하로 입력하세요.")
        String contactNumber,
        @AssertTrue(message = "약관 동의가 필요합니다.")
        boolean agreedToTerms
) {
}