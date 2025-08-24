package com.sleekydz86.domain.service;

import com.sleekydz86.domain.dto.*;
import com.sleekydz86.domain.entity.User;
import com.sleekydz86.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .otpEnabled(false)
                .otpVerified(false)
                .build();

        userRepository.save(user);

        return RegisterResponse.builder()
                .username(user.getUsername())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    public OtpSetupResponse enableOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.isOtpEnabled() && user.isOtpVerified()) {
            throw new RuntimeException("이미 OTP가 활성화되어 있습니다.");
        }

        String secret = otpService.generateSecretKey();
        String qrCode = otpService.generateQRCodeImage(username, secret);

        user.setOtpSecret(secret);
        user.setOtpEnabled(true);
        user.setOtpVerified(false);
        userRepository.save(user);

        return OtpSetupResponse.builder()
                .secret(secret)
                .qrCode("data:image/png;base64," + qrCode)
                .message("QR 코드를 스캔하고 6자리 코드를 입력하여 OTP를 활성화하세요.")
                .build();
    }

    public OtpVerifyResponse verifyOtpSetup(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.isOtpEnabled()) {
            throw new RuntimeException("OTP가 활성화되지 않았습니다.");
        }

        if (otpService.verifyCode(user.getOtpSecret(), code)) {
            user.setOtpVerified(true);
            userRepository.save(user);

            return OtpVerifyResponse.builder()
                    .success(true)
                    .message("OTP가 성공적으로 활성화되었습니다.")
                    .build();
        } else {
            return OtpVerifyResponse.builder()
                    .success(false)
                    .message("잘못된 OTP 코드입니다.")
                    .build();
        }
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        if (user.isOtpEnabled() && user.isOtpVerified()) {
            return LoginResponse.builder()
                    .requireOtp(true)
                    .message("OTP 코드를 입력하세요.")
                    .build();
        }

        return LoginResponse.builder()
                .requireOtp(false)
                .message("로그인 성공")
                .username(user.getUsername())
                .build();
    }

    public LoginResponse verifyOtpLogin(OtpLoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.isOtpEnabled() || !user.isOtpVerified()) {
            throw new RuntimeException("OTP가 설정되지 않았습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        if (otpService.verifyCode(user.getOtpSecret(), request.getOtpCode())) {
            return LoginResponse.builder()
                    .requireOtp(false)
                    .message("OTP 로그인 성공")
                    .username(user.getUsername())
                    .build();
        } else {
            throw new RuntimeException("잘못된 OTP 코드입니다.");
        }
    }

    public OtpVerifyResponse disableOtp(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!user.isOtpEnabled()) {
            throw new RuntimeException("OTP가 활성화되지 않았습니다.");
        }

        if (otpService.verifyCode(user.getOtpSecret(), code)) {
            user.setOtpEnabled(false);
            user.setOtpVerified(false);
            user.setOtpSecret(null);
            userRepository.save(user);

            return OtpVerifyResponse.builder()
                    .success(true)
                    .message("OTP가 비활성화되었습니다.")
                    .build();
        } else {
            return OtpVerifyResponse.builder()
                    .success(false)
                    .message("잘못된 OTP 코드입니다.")
                    .build();
        }
    }
}