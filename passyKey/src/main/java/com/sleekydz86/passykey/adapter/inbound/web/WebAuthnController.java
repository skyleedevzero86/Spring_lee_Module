package com.sleekydz86.passykey.adapter.inbound.web;

import com.sleekydz86.passykey.application.dto.ApiResponse;
import com.sleekydz86.passykey.application.dto.PasskeyRegistrationRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.inbound.CredentialManagementUseCase;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnAuthenticationUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnRegistrationUseCase;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webauthn")
public class WebAuthnController extends BaseController {

    private final WebAuthnRegistrationUseCase registrationUseCase;
    private final WebAuthnAuthenticationUseCase authenticationUseCase;
    private final CredentialManagementUseCase credentialManagementUseCase;

    public WebAuthnController(
            UserUseCase userUseCase,
            WebAuthnRegistrationUseCase registrationUseCase,
            WebAuthnAuthenticationUseCase authenticationUseCase,
            CredentialManagementUseCase credentialManagementUseCase) {
        super(userUseCase);
        this.registrationUseCase = registrationUseCase;
        this.authenticationUseCase = authenticationUseCase;
        this.credentialManagementUseCase = credentialManagementUseCase;
    }

    @PostMapping("/register/options")
    public ResponseEntity<ApiResponse<PublicKeyCredentialCreationOptions>> getRegistrationOptions(
            HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser();
            PublicKeyCredentialCreationOptions options = registrationUseCase.createRegistrationOptions(
                    user, request.getSession());
            return successResponse("등록 옵션 생성 완료", options);
        } catch (Exception e) {
            logger.error("등록 옵션 생성 실패", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "등록 옵션 생성 실패: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> registerCredential(
            @Valid @RequestBody PasskeyRegistrationRequest request,
            HttpServletRequest httpRequest) {
        try {
            User user = getAuthenticatedUser();
            PasskeyRegistrationRequest.PublicKeyData publicKey = request.getPublicKey();
            PasskeyRegistrationRequest.CredentialData credential = publicKey.getCredential();
            PasskeyRegistrationRequest.ResponseData response = credential.getResponse();

            registrationUseCase.registerCredential(
                    user,
                    credential.getId(),
                    response.getAttestationObject(),
                    response.getClientDataJSON(),
                    response.getTransports(),
                    httpRequest.getSession()
            );

            return successResponse("인증서 등록 성공", Map.of("success", true));
        } catch (Exception e) {
            logger.error("인증서 등록 실패", e);
            return errorResponse("인증서 등록 실패: " + e.getMessage());
        }
    }

    @PostMapping("/authenticate/options")
    public ResponseEntity<ApiResponse<PublicKeyCredentialRequestOptions>> getAuthenticationOptions(
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        try {
            User user = getUserByUsernameOrAuthenticated(username);
            PublicKeyCredentialRequestOptions options = authenticationUseCase.createAuthenticationOptions(
                    user, request.getSession());
            return successResponse("인증 옵션 생성 완료", options);
        } catch (Exception e) {
            logger.error("인증 옵션 생성 실패", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "인증 옵션 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/credentials")
    public ResponseEntity<ApiResponse<List<WebAuthnCredential>>> getUserCredentials() {
        try {
            User user = getAuthenticatedUser();
            List<WebAuthnCredential> credentials = credentialManagementUseCase.getUserCredentials(user);
            return successResponse("인증서 조회 완료", credentials);
        } catch (Exception e) {
            logger.error("인증서 조회 실패", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "인증서 조회 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/credentials/{credentialId}")
    public ResponseEntity<ApiResponse<Void>> deleteCredential(@PathVariable String credentialId) {
        try {
            getAuthenticatedUser();
            credentialManagementUseCase.deleteCredential(credentialId);
            return successResponse("인증서 삭제 성공", null);
        } catch (Exception e) {
            logger.error("인증서 삭제 실패", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "인증서 삭제 실패: " + e.getMessage());
        }
    }
}
