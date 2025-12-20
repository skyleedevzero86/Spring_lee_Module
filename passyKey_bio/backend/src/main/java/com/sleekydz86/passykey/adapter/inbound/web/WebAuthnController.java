package com.sleekydz86.passykey.adapter.inbound.web;

import com.sleekydz86.passykey.application.dto.ApiResponse;
import com.sleekydz86.passykey.application.dto.PasskeyRegistrationRequest;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.inbound.CredentialManagementUseCase;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnAuthenticationUseCase;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnRegistrationUseCase;
import com.sleekydz86.passykey.domain.port.outbound.ChallengeServicePort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnOptionsFactoryPort;
import com.sleekydz86.passykey.global.constants.WebAuthnConstants;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.data.client.challenge.Challenge;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webauthn")
public class WebAuthnController extends BaseController {

    private final WebAuthnRegistrationUseCase registrationUseCase;
    private final WebAuthnAuthenticationUseCase authenticationUseCase;
    private final CredentialManagementUseCase credentialManagementUseCase;
    private final ChallengeServicePort challengeService;
    private final WebAuthnOptionsFactoryPort optionsFactory;

    public WebAuthnController(
            UserUseCase userUseCase,
            WebAuthnRegistrationUseCase registrationUseCase,
            WebAuthnAuthenticationUseCase authenticationUseCase,
            CredentialManagementUseCase credentialManagementUseCase,
            ChallengeServicePort challengeService,
            WebAuthnOptionsFactoryPort optionsFactory) {
        super(userUseCase);
        this.registrationUseCase = registrationUseCase;
        this.authenticationUseCase = authenticationUseCase;
        this.credentialManagementUseCase = credentialManagementUseCase;
        this.challengeService = challengeService;
        this.optionsFactory = optionsFactory;
    }

    @PostMapping("/register/options")
    public ResponseEntity<ApiResponse<PublicKeyCredentialCreationOptions>> getRegistrationOptions(
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        try {
            User user;
            try {
                user = getAuthenticatedUser();
            } catch (IllegalStateException e) {
                if (username == null || username.isEmpty()) {
                    return errorResponse(HttpStatus.UNAUTHORIZED, "인증이 필요하거나 사용자명을 제공해야 합니다");
                }
                user = getUserByUsernameOrAuthenticated(username);
            }

            String origin = request.getHeader("Origin");
            String host = extractHostFromOrigin(origin, request);
            String rpId = determineRpId(host);

            PublicKeyCredentialCreationOptions options = registrationUseCase.createRegistrationOptions(
                    user, request.getSession(), rpId);
            return successResponse("등록 옵션 생성 완료", options);
        } catch (Exception e) {
            logger.error("등록 옵션 생성 실패", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "등록 옵션 생성 실패: " + e.getMessage());
        }
    }

    private String extractHostFromOrigin(String origin, HttpServletRequest request) {
        if (origin != null && !origin.isEmpty()) {
            try {
                java.net.URL url = new java.net.URL(origin);
                return url.getHost();
            } catch (Exception e) {
                logger.debug("Origin에서 호스트 추출 실패: {}", origin);
            }
        }

        String host = request.getHeader("Host");
        if (host != null && !host.isEmpty()) {
            if (host.contains(":")) {
                return host.split(":")[0];
            }
            return host;
        }

        return request.getServerName();
    }

    private String determineRpId(String requestHost) {
        if (requestHost != null && !requestHost.isEmpty()) {
            if (requestHost.contains(".ngrok.io") || requestHost.contains(".ngrok-free.app")) {
                return requestHost;
            }
            if (!requestHost.equals("localhost") && !requestHost.startsWith("localhost:")) {
                return requestHost;
            }
        }
        return "localhost";
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
                    publicKey.getLabel(),
                    httpRequest.getSession());

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
            User user = null;
            try {
                user = getAuthenticatedUser();
            } catch (IllegalStateException | com.sleekydz86.passykey.global.exception.UserNotFoundException e) {
                if (username != null && !username.isEmpty()) {
                    String trimmedUsername = username.trim();
                    logger.debug("사용자 검색 시도: {}", trimmedUsername);
                    try {
                        user = userUseCase.findByUsername(trimmedUsername);
                        logger.debug("사용자명으로 사용자 찾음: {}", trimmedUsername);
                    } catch (com.sleekydz86.passykey.global.exception.UserNotFoundException ex) {
                        logger.debug("사용자명으로 사용자 찾기 실패, display_name으로 시도: {}", trimmedUsername);
                        try {
                            user = userUseCase.findByDisplayName(trimmedUsername);
                            logger.debug("display_name으로 사용자 찾음: {}", trimmedUsername);
                        } catch (com.sleekydz86.passykey.global.exception.UserNotFoundException ex2) {
                            logger.warn("인증 옵션 요청 - 존재하지 않는 사용자 (username 또는 display_name): {}", trimmedUsername);
                            return errorResponse(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + trimmedUsername);
                        }
                    }
                }
            }

            if (user == null) {
                logger.debug("사용자가 null이므로 discoverable credentials 옵션 생성");
            }

            String origin = request.getHeader("Origin");
            String host = extractHostFromOrigin(origin, request);
            String rpId = determineRpId(host);

            jakarta.servlet.http.HttpSession session = request.getSession();
            String sessionId = session.getId();
            logger.debug("인증 옵션 생성 - 세션 ID: {}, 사용자: {}, RP ID: {}", sessionId, user != null ? user.getUsername() : "null", rpId);
            
            PublicKeyCredentialRequestOptions options;
            if (user != null) {
                options = authenticationUseCase.createAuthenticationOptions(
                        user, session, rpId);
            } else {
                Challenge challenge = challengeService.generateAndStoreChallenge(
                        sessionId, WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);
                options = optionsFactory.createAuthenticationOptions(challenge, rpId, Collections.emptyList());
            }
            return successResponse("인증 옵션 생성 완료", options);
        } catch (com.sleekydz86.passykey.global.exception.UserNotFoundException e) {
            logger.warn("인증 옵션 생성 실패 - 사용자 없음: {}", username, e);
            return errorResponse(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + (username != null ? username : "알 수 없음"));
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

    @PutMapping("/credentials/{credentialId}/label")
    public ResponseEntity<ApiResponse<Void>> updateCredentialLabel(
            @PathVariable String credentialId,
            @RequestBody Map<String, String> request) {
        try {
            getAuthenticatedUser();
            String label = request.get("label");
            credentialManagementUseCase.updateCredentialLabel(credentialId, label);
            return successResponse("패스키 이름 변경 성공", null);
        } catch (Exception e) {
            logger.error("패스키 이름 변경 실패", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "패스키 이름 변경 실패: " + e.getMessage());
        }
    }
}
