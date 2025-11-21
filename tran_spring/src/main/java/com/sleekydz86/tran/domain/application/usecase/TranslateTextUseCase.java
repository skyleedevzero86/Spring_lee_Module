package com.sleekydz86.tran.domain.application.usecase;

import com.sleekydz86.tran.domain.application.dto.TranslationRequestDto;
import com.sleekydz86.tran.domain.application.dto.TranslationResponseDto;
import com.sleekydz86.tran.domain.model.port.out.TranslationPort;
import com.sleekydz86.tran.domain.model.record.Language;
import com.sleekydz86.tran.domain.model.record.Translation;
import com.sleekydz86.tran.domain.model.record.TranslationRequest;
import com.sleekydz86.tran.global.exception.TranslationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TranslateTextUseCase {
    private static final Logger logger = LoggerFactory.getLogger(TranslateTextUseCase.class);

    private final TranslationPort translationPort;

    public TranslateTextUseCase(TranslationPort translationPort) {
        this.translationPort = translationPort;
    }

    public TranslationResponseDto translate(TranslationRequestDto requestDto) {
        logger.info("번역 요청 시작: {}", requestDto);

        validateTranslationServiceAvailability();

        TranslationRequest domainRequest = convertToDomainRequest(requestDto);

        Translation translation = translationPort.translate(domainRequest);

        TranslationResponseDto response = TranslationResponseDto.from(translation);

        logger.info("번역 완료: {} -> {}",
                translation.getDetectedSourceLanguage().getCode(),
                translation.getTargetLanguage().getCode());

        return response;
    }

    public boolean isServiceAvailable() {
        return translationPort.isAvailable();
    }

    public String getServiceUsageInfo() {
        try {
            return translationPort.getUsageInfo();
        } catch (Exception e) {
            logger.warn("사용량 정보 조회 실패: {}", e.getMessage());
            return "사용량 정보를 가져올 수 없습니다";
        }
    }

    private void validateTranslationServiceAvailability() {
        if (!translationPort.isAvailable()) {
            throw new TranslationException("번역 서비스를 사용할 수 없습니다. API 키를 확인해주세요.");
        }
    }

    private TranslationRequest convertToDomainRequest(TranslationRequestDto dto) {
        Language targetLanguage = Language.of(dto.getTargetLanguage());

        if (dto.isAutoDetect()) {
            return TranslationRequest.autoDetect(dto.getText(), targetLanguage);
        } else {
            Language sourceLanguage = Language.of(dto.getSourceLanguage());
            return TranslationRequest.of(dto.getText(), sourceLanguage, targetLanguage);
        }
    }
}
