package com.sleekydz86.oidstudy.oidc.web;

import com.sleekydz86.oidstudy.global.security.AppOidcUser;
import jakarta.servlet.http.HttpServletRequest;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.web.req.WithdrawRequest;
import com.sleekydz86.oidstudy.oidc.web.resp.LoginIdAvailabilityResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserAccountApplicationService userAccountApplicationService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AccountController(UserAccountApplicationService userAccountApplicationService) {
        this.userAccountApplicationService = userAccountApplicationService;
    }

    @PostMapping("/withdraw")
    public LoginIdAvailabilityResponse withdraw(
            @Valid @RequestBody(required = false) WithdrawRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        AppOidcUser principal = (AppOidcUser) authentication.getPrincipal();
        String reason = request == null ? null : request.reason();
        userAccountApplicationService.withdraw(principal.getAccount().getId(), reason);
        logoutHandler.logout(httpServletRequest, httpServletResponse, authentication);
        SecurityContextHolder.clearContext();
        return new LoginIdAvailabilityResponse(true, null, "탈퇴 처리되었습니다. 이후 동일 아이디 사용은 관리자 문의가 필요합니다.");
    }
}