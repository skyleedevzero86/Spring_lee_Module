package com.sleekydz86.oidstudy.oidc.web;

import java.util.List;
import com.sleekydz86.oidstudy.global.security.AppOidcUser;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.domain.AccountStatus;
import com.sleekydz86.oidstudy.oidc.web.factory.AdminUserResponseFactory;
import com.sleekydz86.oidstudy.oidc.web.req.AdminApprovalRequest;
import com.sleekydz86.oidstudy.oidc.web.resp.AdminUserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserAccountApplicationService userAccountApplicationService;
    private final AdminUserResponseFactory adminUserResponseFactory;

    public AdminUserController(
            UserAccountApplicationService userAccountApplicationService,
            AdminUserResponseFactory adminUserResponseFactory
    ) {
        this.userAccountApplicationService = userAccountApplicationService;
        this.adminUserResponseFactory = adminUserResponseFactory;
    }

    @GetMapping
    public List<AdminUserResponse> users(@RequestParam(name = "status", required = false) AccountStatus status) {
        return userAccountApplicationService.findAll(status).stream()
                .map(adminUserResponseFactory::create)
                .toList();
    }

    @PostMapping("/{userId}/approve")
    public AdminUserResponse approve(
            @PathVariable Long userId,
            @RequestBody(required = false) AdminApprovalRequest request,
            Authentication authentication
    ) {
        AppOidcUser principal = (AppOidcUser) authentication.getPrincipal();
        List<String> requestedRoles = request == null ? List.of() : request.roles();
        return adminUserResponseFactory.create(
                userAccountApplicationService.approve(userId, principal.getAccount().getId(), requestedRoles)
        );
    }

    @PostMapping("/{userId}/reject")
    public AdminUserResponse reject(@PathVariable Long userId, Authentication authentication) {
        AppOidcUser principal = (AppOidcUser) authentication.getPrincipal();
        return adminUserResponseFactory.create(
                userAccountApplicationService.reject(userId, principal.getAccount().getId())
        );
    }
}