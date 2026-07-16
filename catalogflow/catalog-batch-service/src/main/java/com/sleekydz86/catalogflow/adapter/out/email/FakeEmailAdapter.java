package com.sleekydz86.catalogflow.adapter.out.email;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sleekydz86.catalogflow.application.port.out.EmailPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.batch.email-provider", havingValue = "fake", matchIfMissing = true)
public class FakeEmailAdapter implements EmailPort {

	private final List<SentMail> sentMails = new CopyOnWriteArrayList<>();

	@Override
	public void send(String to, String subject, String htmlBody) {
		sentMails.add(new SentMail(to, subject, htmlBody));
	}

	public List<SentMail> getSentMails() {
		return List.copyOf(sentMails);
	}

	public void clear() {
		sentMails.clear();
	}

	public record SentMail(String to, String subject, String htmlBody) {
	}
}
