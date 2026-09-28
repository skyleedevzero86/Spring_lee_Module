package com.sleekydz86.productrecommend.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.chat", name = "enabled", havingValue = "true")
public class ChatClientConfiguration {

	@Bean
	@ConditionalOnBean(ChatClient.Builder.class)
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}
}
