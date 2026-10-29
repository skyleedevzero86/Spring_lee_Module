package com.example.tts.global.config;

import com.example.tts.speech.domain.service.SpeechLanguageResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpeechDomainConfiguration {

    @Bean
    public SpeechLanguageResolver speechLanguageResolver() {
        return new SpeechLanguageResolver();
    }
}
