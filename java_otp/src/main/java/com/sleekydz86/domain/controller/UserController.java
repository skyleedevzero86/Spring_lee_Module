package com.sleekydz86.domain.controller;

import com.sleekydz86.domain.dto.*;
import com.sleekydz86.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/otp/verify-login")
    public ResponseEntity<LoginResponse> verifyOtpLogin(@RequestBody OtpLoginRequest request) {
        return ResponseEntity.ok(userService.verifyOtpLogin(request));
    }

    @PostMapping("/otp/setup/{username}")
    public ResponseEntity<OtpSetupResponse> setupOtp(@PathVariable String username) {
        return ResponseEntity.ok(userService.enableOtp(username));
    }

    @PostMapping("/otp/verify-setup")
    public ResponseEntity<OtpVerifyResponse> verifyOtpSetup(@RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(userService.verifyOtpSetup(request.getUsername(), request.getCode()));
    }

    @PostMapping("/otp/disable")
    public ResponseEntity<OtpVerifyResponse> disableOtp(@RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(userService.disableOtp(request.getUsername(), request.getCode()));
    }
}