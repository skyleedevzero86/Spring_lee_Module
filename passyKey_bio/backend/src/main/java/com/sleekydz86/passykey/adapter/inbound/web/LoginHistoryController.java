package com.sleekydz86.passykey.adapter.inbound.web;

import com.sleekydz86.passykey.application.dto.ApiResponse;
import com.sleekydz86.passykey.domain.model.LoginHistory;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.domain.service.LoginHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth/login-history")
public class LoginHistoryController extends BaseController {

    private final LoginHistoryService loginHistoryService;

    public LoginHistoryController(
            UserUseCase userUseCase,
            LoginHistoryService loginHistoryService) {
        super(userUseCase);
        this.loginHistoryService = loginHistoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoginHistory>>> getLoginHistory(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            User user = getAuthenticatedUser();
            List<LoginHistory> history = loginHistoryService.getLoginHistory(user, limit);
            return successResponse("로그인 이력 조회 완료", history);
        } catch (Exception e) {
            logger.error("로그인 이력 조회 실패", e);
            return errorResponse("로그인 이력 조회 실패: " + e.getMessage());
        }
    }
}

