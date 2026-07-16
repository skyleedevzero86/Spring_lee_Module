package com.sleekydz86.catalogflow.global.config;

import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
public class OllamaAiConfiguration {

	@Bean
	OllamaApi ollamaApi(AiProperties aiProperties) {
		String baseUrl = aiProperties.getOllama().getBaseUrl();
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new ApplicationException("Ollama 기본 URL이 필요합니다");
		}
		return OllamaApi.builder().baseUrl(baseUrl).build();
	}

	@Bean
	OllamaChatModel ollamaChatModel(OllamaApi ollamaApi, AiProperties aiProperties) {
		return OllamaChatModel.builder()
				.ollamaApi(ollamaApi)
				.options(OllamaChatOptions.builder()
						.model(aiProperties.getOllama().getModel())
						.temperature(aiProperties.getOllama().getTemperature())
						.build())
				.build();
	}
}
