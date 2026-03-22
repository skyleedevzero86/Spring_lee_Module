package com.sleekydz86.oidstudy.oidc.web.req;

import jakarta.validation.constraints.Size;

public record WithdrawRequest(
        @Size(max = 500, message = "탈퇴 사유는 500자 이하로 입력하세요.")
        String reason
) {
}