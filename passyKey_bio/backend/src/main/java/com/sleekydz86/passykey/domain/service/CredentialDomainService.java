package com.sleekydz86.passykey.domain.service;

import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.global.exception.InvalidCounterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CredentialDomainService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialDomainService.class);

    public void validateAndUpdateCounter(WebAuthnCredential credential, Long newCounter) {
        Long currentCounter = credential.getCounter();

        if (currentCounter == null) {
            currentCounter = 0L;
        }

        logger.debug("카운터 검증 - 현재: {}, 새로운: {}", currentCounter, newCounter);

        if (newCounter == null) {
            logger.warn("새 카운터 값이 null입니다. 현재 카운터 유지: {}", currentCounter);
            credential.updateLastUsed();
            return;
        }

        if (newCounter < currentCounter) {
            logger.error("카운터가 감소했습니다. 현재: {}, 새로운: {}", currentCounter, newCounter);
            throw new InvalidCounterException("카운터는 현재 값보다 작을 수 없습니다. 현재: " + currentCounter + ", 새로운: " + newCounter);
        }

        if (newCounter.equals(currentCounter)) {
            if (currentCounter == 0L) {
                logger.info("첫 인증: 카운터가 0에서 0으로 유지됩니다. 허용합니다.");
            } else {
                logger.warn("카운터가 동일합니다. 현재: {}, 새로운: {}. 리플레이 공격 가능성을 확인하세요.", currentCounter, newCounter);
            }
            credential.setCounter(newCounter);
            credential.updateLastUsed();
            return;
        }

        credential.setCounter(newCounter);
        credential.updateLastUsed();
        logger.debug("카운터 업데이트 완료: {} -> {}", currentCounter, newCounter);
    }
}
