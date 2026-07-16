package com.sleekydz86.catalogflow.global.config;

import com.google.genai.Client;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiAiConfiguration {

	@Bean(destroyMethod = "close")
	Client googleGenAiClient(AiProperties aiProperties) {
		String apiKey = aiProperties.getGemini().getApiKey();
		if (apiKey == null || apiKey.isBlank()) {
			apiKey = System.getenv("GOOGLE_API_KEY");
		}
		if (apiKey == null || apiKey.isBlank()) {
			throw new ApplicationException("Gemini API 키가 필요합니다");
		}
		return Client.builder().apiKey(apiKey).build();
	}

	@Bean
	GoogleGenAiChatModel googleGenAiChatModel(Client googleGenAiClient, AiProperties aiProperties) {
		return GoogleGenAiChatModel.builder()
				.genAiClient(googleGenAiClient)
				.options(GoogleGenAiChatOptions.builder()
						.model(aiProperties.getGemini().getModel())
						.temperature(aiProperties.getGemini().getTemperature())
						.build())
				.build();
	}
}
