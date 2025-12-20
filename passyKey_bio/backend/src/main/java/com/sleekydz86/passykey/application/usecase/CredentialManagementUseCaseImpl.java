package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.inbound.CredentialManagementUseCase;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnCredentialRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CredentialManagementUseCaseImpl implements CredentialManagementUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CredentialManagementUseCaseImpl.class);

    private final WebAuthnCredentialRepositoryPort credentialRepository;

    public CredentialManagementUseCaseImpl(WebAuthnCredentialRepositoryPort credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebAuthnCredential> getUserCredentials(User user) {
        return credentialRepository.findByUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCredential(String credentialId) {
        try {
            credentialRepository.deleteByCredentialId(credentialId);
            logger.info("인증서 삭제됨: {}", credentialId);
        } catch (Exception e) {
            logger.error("인증서 삭제 실패: {}", credentialId, e);
            throw new RuntimeException("인증서 삭제 실패", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCredentialLabel(String credentialId, String label) {
        try {
            WebAuthnCredential credential = credentialRepository.findByCredentialId(credentialId)
                    .orElseThrow(() -> new RuntimeException("인증서를 찾을 수 없습니다: " + credentialId));
            
            if (label != null && !label.trim().isEmpty()) {
                String trimmedLabel = label.trim();
                List<WebAuthnCredential> userCredentials = credentialRepository.findByUser(credential.getUser());
                boolean labelExists = userCredentials.stream()
                        .filter(cred -> !cred.getCredentialId().equals(credentialId))
                        .anyMatch(cred -> cred.getLabel() != null && cred.getLabel().trim().equals(trimmedLabel));
                if (labelExists) {
                    throw new RuntimeException("이미 사용 중인 패스키 이름입니다. 다른 이름을 사용해주세요.");
                }
                credential.setLabel(trimmedLabel);
            } else {
                credential.setLabel(null);
            }
            
            credentialRepository.updateLabel(credential);
            logger.info("인증서 label 업데이트됨: {}, label: {}", credentialId, label);
        } catch (Exception e) {
            logger.error("인증서 label 업데이트 실패: {}", credentialId, e);
            throw new RuntimeException("인증서 label 업데이트 실패: " + e.getMessage(), e);
        }
    }
}
