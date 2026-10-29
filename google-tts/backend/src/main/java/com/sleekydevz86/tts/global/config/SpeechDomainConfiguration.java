package com.sleekydevz86.tts.global.config;

import com.sleekydevz86.tts.speech.domain.service.SpeechLanguageResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpeechDomainConfiguration {

    @Bean
    public SpeechLanguageResolver speechLanguageResolver() {
        return new SpeechLanguageResolver();
    }
}
