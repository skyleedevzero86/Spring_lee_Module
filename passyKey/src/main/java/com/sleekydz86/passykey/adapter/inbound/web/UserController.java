package com.sleekydz86.passykey.adapter.inbound.web;

import com.sleekydz86.passykey.application.dto.ApiResponse;
import com.sleekydz86.passykey.application.dto.RegisterRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class UserController extends BaseController {

    public UserController(UserUseCase userUseCase) {
        super(userUseCase);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userUseCase.register(request);
            return createdResponse("사용자 등록 성공", user);
        } catch (IllegalArgumentException e) {
            logger.error("사용자 등록 실패", e);
            return errorResponse(e.getMessage());
        } catch (Exception e) {
            logger.error("사용자 등록 실패", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 등록 실패: " + e.getMessage());
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userUseCase.existsByUsername(username);
        return successResponse("사용자명 확인 완료", exists);
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userUseCase.existsByEmail(email);
        return successResponse("이메일 확인 완료", exists);
    }
}
