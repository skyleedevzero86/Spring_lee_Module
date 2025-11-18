package com.sleekydz86.payment2v2.domain.member.adapter.in.web;

import com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto.LoginApiResponse;
import com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto.LoginRequest;
import com.sleekydz86.payment2v2.domain.member.application.dto.LoginResponse;
import com.sleekydz86.payment2v2.domain.member.application.port.in.LoginUseCase;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final LoginUseCase loginUseCase;
    private final MemberWebMapper memberWebMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginApiResponse> login(@Valid @RequestBody LoginRequest request) {
        return LoggingUtil.executeWithContext("email", request.getEmail() != null ? request.getEmail() : "알수없음", () -> {
            LoginResponse response = loginUseCase.login(memberWebMapper.toCommand(request));
            LoginApiResponse apiResponse = memberWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        });
    }
}
