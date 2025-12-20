package com.sleekydz86.passykey.adapter.outbound.persistence;

import com.sleekydz86.passykey.adapter.outbound.service.CredentialCacheService;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnCredentialRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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
            logger.debug("인증서 저장 중 - 작업: {}, id: {}, credentialId: {}", 
                    operation, credential.getId(), credential.getCredentialId());
            
            Map<String, Object> params = new HashMap<>();

            params.put("operation", operation);
            params.put("id", credential.getId());
            params.put("credentialId", credential.getCredentialId());
            params.put("publicKeyCose", credential.getPublicKeyCose());
            params.put("counter", credential.getCounter());
            params.put("transports", credential.getTransports());
            params.put("label", credential.getLabel());
            params.put("userId", credential.getUser() != null ? credential.getUser().getId() : null);
            
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            params.put("createdAt", credential.getCreatedAt() != null ? credential.getCreatedAt() : now);
            params.put("lastUsedAt", credential.getLastUsedAt() != null ? credential.getLastUsedAt() : now);

            params.put("resultId", null);

            try {
            credentialMapper.save(params);
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    logger.warn("중복 키 오류 발생, UPDATE로 재시도: credentialId={}, id={}", 
                            credential.getCredentialId(), credential.getId());
                    
                    if (credential.getId() == null) {
                        WebAuthnCredential existing = credentialMapper.selectByCredentialId(credential.getCredentialId());
                        if (existing != null && existing.getId() != null) {
                            credential.setId(existing.getId());
                            operation = "U";
                            params.put("operation", operation);
                            params.put("id", credential.getId());
                            credentialMapper.save(params);
                        } else {
                            throw new IllegalArgumentException("인증서를 찾을 수 없습니다", e);
                        }
                    } else {
                        operation = "U";
                        params.put("operation", operation);
                        credentialMapper.save(params);
                    }
                } else {
                    throw e;
                }
            }

            if (credential.getId() == null && params.get("resultId") != null) {
                Object resultIdValue = params.get("resultId");
                if (resultIdValue instanceof Number) {
                    Long savedId = ((Number) resultIdValue).longValue();
                    credential.setId(savedId);
                    
                    WebAuthnCredential savedCredential = credentialMapper.selectById(savedId);
                    if (savedCredential != null) {
                        if (savedCredential.getCreatedAt() != null) {
                            credential.setCreatedAt(savedCredential.getCreatedAt());
                        }
                        if (savedCredential.getLastUsedAt() != null) {
                            credential.setLastUsedAt(savedCredential.getLastUsedAt());
                        }
                    }
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
    @Transactional(rollbackFor = Exception.class)
    public WebAuthnCredential update(WebAuthnCredential credential) {
        try {
            if (credential.getId() == null) {
                throw new IllegalArgumentException("인증서 ID가 없습니다. 업데이트할 수 없습니다.");
            }

            logger.debug("인증서 업데이트 중 - id: {}, credentialId: {}, counter: {}", 
                    credential.getId(), credential.getCredentialId(), credential.getCounter());

            Map<String, Object> params = new HashMap<>();
            params.put("operation", "U");
            params.put("id", credential.getId());
            params.put("credentialId", null);
            params.put("publicKeyCose", null);
            params.put("counter", credential.getCounter());
            params.put("transports", null);
            params.put("label", null);
            params.put("userId", null);
            params.put("createdAt", null);
            params.put("lastUsedAt", credential.getLastUsedAt() != null ? credential.getLastUsedAt() : java.time.LocalDateTime.now());
            params.put("resultId", null);

            credentialMapper.save(params);

            if (credential.getUser() != null && credential.getUser().getId() != null) {
                credentialCacheService.evictUserCredentials(credential.getUser().getId());
            }

            logger.debug("인증서 업데이트 성공 - id: {}", credential.getId());
            return credential;
        } catch (DataAccessException e) {
            logger.error("인증서 업데이트 중 데이터베이스 오류 발생: {}", credential.getCredentialId(), e);
            throw new RuntimeException("인증서 업데이트 실패", e);
        } catch (Exception e) {
            logger.error("인증서 업데이트 중 예상치 못한 오류 발생: {}", credential.getCredentialId(), e);
            throw new RuntimeException("인증서 업데이트 실패", e);
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
    public void updateLabel(WebAuthnCredential credential) {
        try {
            if (credential.getId() == null) {
                throw new IllegalArgumentException("인증서 ID가 없습니다. 업데이트할 수 없습니다.");
            }

            logger.debug("인증서 label 업데이트 중 - id: {}, credentialId: {}, label: {}", 
                    credential.getId(), credential.getCredentialId(), credential.getLabel());

            Map<String, Object> params = new HashMap<>();
            params.put("operation", "U");
            params.put("id", credential.getId());
            params.put("credentialId", null);
            params.put("publicKeyCose", null);
            params.put("counter", null);
            params.put("transports", null);
            params.put("label", credential.getLabel());
            params.put("userId", null);
            params.put("createdAt", null);
            params.put("lastUsedAt", null);
            params.put("resultId", null);

            credentialMapper.save(params);

            if (credential.getUser() != null && credential.getUser().getId() != null) {
                credentialCacheService.evictUserCredentials(credential.getUser().getId());
            }

            logger.debug("인증서 label 업데이트 성공 - id: {}", credential.getId());
        } catch (DataAccessException e) {
            logger.error("인증서 label 업데이트 중 데이터베이스 오류 발생: {}", credential.getCredentialId(), e);
            throw new RuntimeException("인증서 label 업데이트 실패", e);
        } catch (Exception e) {
            logger.error("인증서 label 업데이트 중 예상치 못한 오류 발생: {}", credential.getCredentialId(), e);
            throw new RuntimeException("인증서 label 업데이트 실패", e);
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
