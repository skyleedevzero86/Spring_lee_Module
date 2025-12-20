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
            User user = authenticationUseCase.authenticate(
                    request.getId(),
                    request.getResponse().getAuthenticatorData(),
                    request.getResponse().getClientDataJSON(),
                    request.getResponse().getSignature(),
                    request.getResponse().getUserHandle(),
                    httpRequest.getSession()
            );

            authenticationService.setAuthentication(user, httpRequest);

            AuthenticationResponse response = new AuthenticationResponse(
                    AuthConstants.REDIRECT_DASHBOARD,
                    true
            );
            return successResponse("인증 성공", response);
        } catch (Exception e) {
            logger.error("인증 실패", e);
            return errorResponse(HttpStatus.UNAUTHORIZED, "인증 실패: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authenticationService.clearAuthentication();
        return successResponse("로그아웃 성공", null);
    }
}
