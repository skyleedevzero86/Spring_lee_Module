package com.sleekydz86.tran.global.config;

import com.deepl.api.Translator;
import com.deepl.api.TranslatorOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeepLConfig {

    private static final Logger logger = LoggerFactory.getLogger(DeepLConfig.class);

    @Value("${deepl.api.key:}")
    private String apiKey;

    @Value("${deepl.api.base-url:https://api-free.deepl.com/v2}")
    private String baseUrl;

    @Value("${deepl.api.enabled:true}")
    private boolean enabled;

    @Bean
    public Translator translator() {
        if (!enabled) {
            logger.warn("DeepL API가 비활성화되었습니다 (deepl.api.enabled=false)");
            return null;
        }

        if (apiKey == null || apiKey.isBlank()) {
            logger.error("DeepL API 키가 설정되지 않았습니다. application.yml에 deepl.api.key를 설정해주세요.");
            return null;
        }

        try {
            TranslatorOptions options = new TranslatorOptions()
                    .setServerUrl(baseUrl)
                    .setSendPlatformInfo(false)
                    .setTimeout(java.time.Duration.ofSeconds(10));

            Translator translator = new Translator(apiKey, options);

            logger.info("DeepL Translator 초기화 완료");
            logger.info("- Base URL: {}", baseUrl);
            logger.info("- API 키: {}***", apiKey.substring(0, Math.min(8, apiKey.length())));

            try {
                var usage = translator.getUsage();
                if (usage.getCharacter() != null) {
                    logger.info("- 현재 사용량: {} 문자", usage.getCharacter().getCount());
                    Long limit = usage.getCharacter().getLimit();
                    if (limit != null) {
                        logger.info("- 사용 한도: {} 문자", limit);
                    }
                }
            } catch (Exception e) {
                logger.warn("사용량 확인 실패 (API 키는 유효함): {}", e.getMessage());
            }

            return translator;

        } catch (Exception e) {
            logger.error("DeepL Translator 초기화 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    @Bean
    public boolean deeplEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

}
