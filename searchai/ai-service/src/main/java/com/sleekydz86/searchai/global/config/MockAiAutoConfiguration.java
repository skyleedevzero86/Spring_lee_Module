package com.sleekydz86.searchai.global.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "mock", matchIfMissing = true)
@EnableAutoConfiguration(excludeName = {
	"org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration",
	"org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration",
	"org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration",
	"org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration",
	"org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration",
	"org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration",
	"org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration"
})
public class MockAiAutoConfiguration {
}
