package com.sleekydz86.passykey.adapter.inbound.web;

import com.sleekydz86.passykey.global.security.AuthenticationService;
import com.sleekydz86.passykey.application.dto.ApiResponse;
import com.sleekydz86.passykey.application.dto.AuthenticationResponse;
import com.sleekydz86.passykey.application.dto.PasskeyAuthenticationRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnAuthenticationUseCase;
import com.sleekydz86.passykey.global.constants.AuthConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    private final WebAuthnAuthenticationUseCase authenticationUseCase;
    private final AuthenticationService authenticationService;

    public AuthController(
            UserUseCase userUseCase,
            WebAuthnAuthenticationUseCase authenticationUseCase,
            AuthenticationService authenticationService) {
        super(userUseCase);
        this.authenticationUseCase = authenticationUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/webauthn/authenticate")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticateWithPasskey(
            @Valid @RequestBody PasskeyAuthenticationRequest request,
            HttpServletRequest httpRequest) {
        try {
            jakarta.servlet.http.HttpSession session = httpRequest.getSession(false);
            if (session == null) {
                logger.warn("인증 시도 - 세션이 없습니다. 인증 옵션을 먼저 요청해주세요.");
                return errorResponse(HttpStatus.BAD_REQUEST, "세션이 없습니다. 인증 옵션을 먼저 요청해주세요.");
            }
            
            logger.debug("인증 시도 - 세션 ID: {}", session.getId());
            
            User user = authenticationUseCase.authenticate(
                    request.getId(),
                    request.getResponse().getAuthenticatorData(),
                    request.getResponse().getClientDataJSON(),
                    request.getResponse().getSignature(),
                    request.getResponse().getUserHandle(),
                    session
            );

            try {
                authenticationService.checkAndPreventDuplicateLogin(user, httpRequest);
            } catch (IllegalStateException e) {
                logger.warn("중복 로그인 시도 차단: {}", e.getMessage());
                return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
            }

            authenticationService.setAuthentication(user, httpRequest, "PASSKEY");

            AuthenticationResponse response = new AuthenticationResponse(
                    AuthConstants.REDIRECT_DASHBOARD,
                    true,
                    true
            );
            return successResponse("인증 성공", response);
        } catch (IllegalStateException e) {
            logger.warn("중복 로그인 시도 차단: {}", e.getMessage());
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            logger.error("인증 실패", e);
            return errorResponse(HttpStatus.UNAUTHORIZED, "인증 실패: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authenticationService.clearAuthentication(request);
        return successResponse("로그아웃 성공", null);
    }
}
