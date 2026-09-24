package com.sleekydz86.searchai.notification.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
public final class NotifyController {

	private static final Logger log = LoggerFactory.getLogger(NotifyController.class);

	private final RestClient restClient = RestClient.create();
	private final boolean telegramEnabled;
	private final String telegramBotToken;
	private final String telegramChatId;
	private final boolean slackEnabled;
	private final String slackWebhookUrl;

	public NotifyController(
		@Value("${app.notification.telegram-enabled:false}") boolean telegramEnabled,
		@Value("${app.notification.telegram-bot-token:}") String telegramBotToken,
		@Value("${app.notification.telegram-chat-id:}") String telegramChatId,
		@Value("${app.notification.slack-enabled:false}") boolean slackEnabled,
		@Value("${app.notification.slack-webhook-url:}") String slackWebhookUrl
	) {
		this.telegramEnabled = telegramEnabled;
		this.telegramBotToken = telegramBotToken;
		this.telegramChatId = telegramChatId;
		this.slackEnabled = slackEnabled;
		this.slackWebhookUrl = slackWebhookUrl;
	}

	@PostMapping("/notify")
	public ResponseEntity<Map<String, String>> notify(@RequestBody NotifyRequest request) {
		log.info("알림 수신: [{}] {} — {}", request.severity(), request.title(), request.body());
		if (telegramEnabled) {
			try {
				restClient.post()
					.uri("https://api.telegram.org/bot{token}/sendMessage", telegramBotToken)
					.body(Map.of("chat_id", telegramChatId, "text", "[" + request.severity() + "] " + request.title() + "\n" + request.body()))
					.retrieve()
					.toBodilessEntity();
			} catch (Exception ex) {
				log.warn("Telegram 실패: {}", ex.getMessage());
			}
		}
		if (slackEnabled) {
			try {
				restClient.post()
					.uri(slackWebhookUrl)
					.body(Map.of("text", "*" + request.severity() + "* `" + request.title() + "`\n" + request.body()))
					.retrieve()
					.toBodilessEntity();
			} catch (Exception ex) {
				log.warn("Slack 실패: {}", ex.getMessage());
			}
		}
		return ResponseEntity.accepted().body(Map.of("status", "accepted"));
	}

	public record NotifyRequest(String title, String body, String severity) {
	}
}
