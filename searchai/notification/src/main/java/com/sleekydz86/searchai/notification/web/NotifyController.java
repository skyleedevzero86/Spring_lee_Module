package com.sleekydz86.searchai.notification.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
	private final boolean emailEnabled;
	private final String emailTo;
	private final String emailFrom;
	private final String emailWebhookUrl;

	public NotifyController(
		@Value("${app.notification.telegram-enabled:false}") boolean telegramEnabled,
		@Value("${app.notification.telegram-bot-token:}") String telegramBotToken,
		@Value("${app.notification.telegram-chat-id:}") String telegramChatId,
		@Value("${app.notification.slack-enabled:false}") boolean slackEnabled,
		@Value("${app.notification.slack-webhook-url:}") String slackWebhookUrl,
		@Value("${app.notification.email-enabled:false}") boolean emailEnabled,
		@Value("${app.notification.email-to:}") String emailTo,
		@Value("${app.notification.email-from:searchai@localhost}") String emailFrom,
		@Value("${app.notification.email-webhook-url:}") String emailWebhookUrl
	) {
		this.telegramEnabled = telegramEnabled;
		this.telegramBotToken = telegramBotToken == null ? "" : telegramBotToken.trim();
		this.telegramChatId = telegramChatId == null ? "" : telegramChatId.trim();
		this.slackEnabled = slackEnabled;
		this.slackWebhookUrl = slackWebhookUrl == null ? "" : slackWebhookUrl.trim();
		this.emailEnabled = emailEnabled;
		this.emailTo = emailTo == null ? "" : emailTo.trim();
		this.emailFrom = emailFrom == null || emailFrom.isBlank() ? "searchai@localhost" : emailFrom.trim();
		this.emailWebhookUrl = emailWebhookUrl == null ? "" : emailWebhookUrl.trim();
	}

	@PostMapping("/notify")
	public ResponseEntity<Map<String, Object>> notify(@RequestBody NotifyRequest request) {
		if (request == null || request.title() == null || request.title().isBlank()) {
			log.warn("알림 요청 거부: 제목이 비어 있습니다");
			return ResponseEntity.badRequest().body(Map.of(
				"status", "rejected",
				"reason", "제목이 비어 있습니다"
			));
		}
		String severity = request.severity() == null || request.severity().isBlank() ? "INFO" : request.severity();
		String body = request.body() == null ? "" : request.body();
		log.info("알림 수신: 심각도={} 제목={} 본문길이={}", severity, request.title(), body.length());

		List<String> succeeded = new ArrayList<>();
		List<String> failed = new ArrayList<>();
		boolean anyChannel = false;

		if (telegramEnabled) {
			anyChannel = true;
			if (telegramBotToken.isBlank() || telegramChatId.isBlank()) {
				failed.add("telegram");
				log.warn("텔레그램 채널 설정 누락: 토큰 또는 chatId가 비어 있습니다");
			} else if (sendTelegram(request.title(), body, severity)) {
				succeeded.add("telegram");
			} else {
				failed.add("telegram");
			}
		}
		if (slackEnabled) {
			anyChannel = true;
			if (slackWebhookUrl.isBlank()) {
				failed.add("slack");
				log.warn("슬랙 채널 설정 누락: webhook URL이 비어 있습니다");
			} else if (sendSlack(request.title(), body, severity)) {
				succeeded.add("slack");
			} else {
				failed.add("slack");
			}
		}
		if (emailEnabled) {
			anyChannel = true;
			if (emailTo.isBlank() || emailWebhookUrl.isBlank()) {
				failed.add("email");
				log.warn("이메일 채널 설정 누락: 수신자 또는 webhook URL이 비어 있습니다");
			} else if (sendEmail(request.title(), body, severity)) {
				succeeded.add("email");
			} else {
				failed.add("email");
			}
		}

		if (!anyChannel) {
			log.warn("활성화된 알림 채널이 없습니다. 요청만 기록합니다. 제목={}", request.title());
		}

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("status", failed.isEmpty() ? "accepted" : (succeeded.isEmpty() ? "failed" : "partial"));
		response.put("succeeded", succeeded);
		response.put("failed", failed);
		if (failed.isEmpty()) {
			return ResponseEntity.accepted().body(response);
		}
		if (succeeded.isEmpty()) {
			return ResponseEntity.status(502).body(response);
		}
		return ResponseEntity.status(207).body(response);
	}

	private boolean sendTelegram(String title, String body, String severity) {
		try {
			String text = "[" + severity + "] " + title + "\n" + body;
			if (text.length() > 4000) {
				text = text.substring(0, 3997) + "...";
			}
			restClient.post()
				.uri("https://api.telegram.org/bot{token}/sendMessage", telegramBotToken)
				.body(Map.of("chat_id", telegramChatId, "text", text))
				.retrieve()
				.toBodilessEntity();
			log.info("텔레그램 전송 성공: chatId={} 제목={}", telegramChatId, title);
			return true;
		} catch (RestClientException ex) {
			log.error("텔레그램 전송 실패: 원인={}", ex.getMessage(), ex);
			return false;
		} catch (Exception ex) {
			log.error("텔레그램 처리 예외: 원인={}", ex.getMessage(), ex);
			return false;
		}
	}

	private boolean sendSlack(String title, String body, String severity) {
		try {
			restClient.post()
				.uri(slackWebhookUrl)
				.body(Map.of("text", "*" + severity + "* `" + title + "`\n" + body))
				.retrieve()
				.toBodilessEntity();
			log.info("슬랙 전송 성공: 제목={}", title);
			return true;
		} catch (RestClientException ex) {
			log.error("슬랙 전송 실패: 원인={}", ex.getMessage(), ex);
			return false;
		} catch (Exception ex) {
			log.error("슬랙 처리 예외: 원인={}", ex.getMessage(), ex);
			return false;
		}
	}

	private boolean sendEmail(String title, String body, String severity) {
		try {
			restClient.post()
				.uri(emailWebhookUrl)
				.body(Map.of(
					"from", emailFrom,
					"to", emailTo,
					"subject", "[" + severity + "] " + title,
					"text", body,
					"severity", severity
				))
				.retrieve()
				.toBodilessEntity();
			log.info("이메일 전송 성공: to={} 제목={}", emailTo, title);
			return true;
		} catch (RestClientException ex) {
			log.error("이메일 전송 실패: 원인={}", ex.getMessage(), ex);
			return false;
		} catch (Exception ex) {
			log.error("이메일 처리 예외: 원인={}", ex.getMessage(), ex);
			return false;
		}
	}

	public record NotifyRequest(String title, String body, String severity) {
	}
}
