package com.sleekydz86.passykey.adapter.outbound.persistence;

import com.sleekydz86.passykey.adapter.outbound.service.CredentialCacheService;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnCredentialRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class WebAuthnCredentialRepositoryAdapter implements WebAuthnCredentialRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(WebAuthnCredentialRepositoryAdapter.class);

    private final WebAuthnCredentialMyBatisMapper credentialMapper;
    private final CredentialCacheService credentialCacheService;

    public WebAuthnCredentialRepositoryAdapter(
            WebAuthnCredentialMyBatisMapper credentialMapper,
            CredentialCacheService credentialCacheService) {
        this.credentialMapper = credentialMapper;
        this.credentialCacheService = credentialCacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebAuthnCredential save(WebAuthnCredential credential) {
        try {
            String operation = (credential.getId() == null) ? "C" : "U";
            Map<String, Object> params = new HashMap<>();

            params.put("operation", operation);
            params.put("id", credential.getId());
            params.put("credentialId", credential.getCredentialId());
            params.put("publicKeyCose", credential.getPublicKeyCose());
            params.put("counter", credential.getCounter());
            params.put("transports", credential.getTransports());
            params.put("label", credential.getLabel());
            params.put("userId", credential.getUser() != null ? credential.getUser().getId() : null);
            params.put("createdAt", credential.getCreatedAt());
            params.put("lastUsedAt", credential.getLastUsedAt());

            params.put("resultId", null);

            credentialMapper.save(params);

            if (credential.getId() == null && params.get("resultId") != null) {
                Object resultIdValue = params.get("resultId");
                if (resultIdValue instanceof Number) {
                    credential.setId(((Number) resultIdValue).longValue());
                }
            }

            if (credential.getUser() != null && credential.getUser().getId() != null) {
                credentialCacheService.evictUserCredentials(credential.getUser().getId());
            }
            return credential;
        } catch (DuplicateKeyException e) {
            logger.error("인증서 저장 중 중복 키 오류 발생: {}", credential.getCredentialId(), e);
            throw new IllegalArgumentException("이미 존재하는 인증서입니다", e);
        } catch (DataAccessException e) {
            logger.error("인증서 저장 중 데이터베이스 오류 발생: {}", credential.getCredentialId(), e);
            throw new RuntimeException("인증서 저장 실패", e);
        } catch (Exception e) {
            logger.error("인증서 저장 중 예상치 못한 오류 발생: {}", credential.getCredentialId(), e);
            throw new RuntimeException("인증서 저장 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WebAuthnCredential> findByCredentialId(String credentialId) {
        try {
            WebAuthnCredential credential = credentialMapper.selectByCredentialId(credentialId);
            return Optional.ofNullable(credential);
        } catch (DataAccessException e) {
            logger.error("인증서 조회 중 데이터베이스 오류 발생: {}", credentialId, e);
            throw new RuntimeException("인증서 조회 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebAuthnCredential> findByUser(User user) {
        try {
            List<WebAuthnCredential> cached = credentialCacheService.getUserCredentialsFromCache(user.getId());
            if (cached != null) {
                return cached;
            }
            List<WebAuthnCredential> credentials = credentialMapper.selectByUserId(user.getId());
            if (credentials != null && !credentials.isEmpty()) {
                credentialCacheService.cacheUserCredentials(user.getId(), credentials);
            }
            return credentials;
        } catch (DataAccessException e) {
            logger.error("사용자 인증서 목록 조회 중 데이터베이스 오류 발생: {}", user.getId(), e);
            throw new RuntimeException("인증서 목록 조회 실패", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCredentialId(String credentialId) {
        try {
            WebAuthnCredential credential = credentialMapper.selectByCredentialId(credentialId);
            if (credential != null && credential.getUser() != null && credential.getUser().getId() != null) {
                credentialCacheService.evictUserCredentials(credential.getUser().getId());
            }
            credentialMapper.deleteByCredentialId(credentialId);
        } catch (DataAccessException e) {
            logger.error("인증서 삭제 중 데이터베이스 오류 발생: {}", credentialId, e);
            throw new RuntimeException("인증서 삭제 실패", e);
        } catch (Exception e) {
            logger.error("인증서 삭제 중 예상치 못한 오류 발생: {}", credentialId, e);
            throw new RuntimeException("인증서 삭제 실패", e);
        }
    }
}
