package com.sleekydz86.tran.domain.adapter.out.translation;

import com.deepl.api.*;
import com.sleekydz86.tran.domain.model.port.out.TranslationPort;
import com.sleekydz86.tran.domain.model.record.Language;
import com.sleekydz86.tran.domain.model.record.Translation;
import com.sleekydz86.tran.domain.model.record.TranslationRequest;
import com.sleekydz86.tran.global.exception.TranslationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeepLTranslationAdapter  implements TranslationPort {

    private static final Logger logger = LoggerFactory.getLogger(DeepLTranslationAdapter.class);

    private final Translator translator;
    private final boolean enabled;

    public DeepLTranslationAdapter(Translator translator, boolean enabled) {
        this.translator = translator;
        this.enabled = enabled;
    }

    @Override
    public Translation translate(TranslationRequest request) {
        if (!isAvailable()) {
            throw new TranslationException("번역 서비스를 사용할 수 없습니다");
        }

        try {
            logger.debug("DeepL 번역 시작: {}", request);

            TextTranslationOptions options = new TextTranslationOptions();

            String sourceLanguageCode = request.isAutoDetect() ?
                    null : request.getSourceLanguage().getCode();

            String targetLanguageCode = request.getTargetLanguage().getCode();

            TextResult result = translator.translateText(
                    request.getSourceText(),
                    sourceLanguageCode,
                    targetLanguageCode,
                    options
            );

            String detectedLanguageCode = result.getDetectedSourceLanguage();
            Language detectedSourceLanguage = Language.of(detectedLanguageCode);

            Translation translation = Translation.of(
                    request.getSourceText(),
                    result.getText(),
                    detectedSourceLanguage,
                    request.getTargetLanguage()
            );

            logger.info("DeepL 번역 완료: {} -> {}, 원본 길이: {}, 번역 길이: {}",
                    detectedLanguageCode,
                    targetLanguageCode,
                    request.getSourceText().length(),
                    result.getText().length());

            return translation;

        } catch (DeepLException e) {
            logger.error("DeepL API 오류: {}", e.getMessage(), e);
            throw new TranslationException("번역 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("번역 요청이 중단되었습니다: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new TranslationException("번역 요청이 중단되었습니다", e);
        } catch (Exception e) {
            logger.error("예상치 못한 오류: {}", e.getMessage(), e);
            throw new TranslationException("번역 중 예상치 못한 오류가 발생했습니다", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && translator != null;
    }

    @Override
    public String getUsageInfo() {
        if (!isAvailable()) {
            return "번역 서비스를 사용할 수 없습니다";
        }

        try {
            Usage usage = translator.getUsage();

            if (usage.getCharacter() != null) {
                long used = usage.getCharacter().getCount();
                Long limit = usage.getCharacter().getLimit();

                if (limit != null && limit > 0) {
                    double percentage = (used * 100.0) / limit;
                    return String.format("사용량: %,d / %,d 문자 (%.1f%%)", used, limit, percentage);
                } else {
                    return String.format("사용량: %,d 문자", used);
                }
            }

            return "사용량 정보를 확인할 수 없습니다";

        } catch (Exception e) {
            logger.warn("사용량 조회 실패: {}", e.getMessage());
            return "사용량 정보 조회 실패";
        }
    }
}